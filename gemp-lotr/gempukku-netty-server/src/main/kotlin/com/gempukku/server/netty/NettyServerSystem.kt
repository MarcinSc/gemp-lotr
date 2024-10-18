package com.gempukku.server.netty

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectProperty
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.server.BanChecker
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServerSystem
import com.gempukku.server.ResponseWriter
import com.gempukku.server.ServerRequestHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpContentCompressor
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import java.util.*

@Exposes(LifecycleObserver::class, HttpServerSystem::class, UpdatedSystem::class)
class NettyServerSystem : LifecycleObserver, HttpServerSystem, UpdatedSystem {

    @InjectProperty("server.netty.port")
    private var port: Int = 8080

    @Inject(allowsNull = true)
    private var banChecker: BanChecker? = null

    private val pendingRequests: MutableList<PendingRequest> = Collections.synchronizedList(mutableListOf())

    private val registrations: MutableList<Registration> = mutableListOf()

    private var bossGroup: NioEventLoopGroup? = null
    private var workerGroup: NioEventLoopGroup? = null
    private var serverChannel: Channel? = null

    override fun registerRequestHandler(
        method: HttpMethod,
        uriRegex: String,
        requestHandler: ServerRequestHandler
    ): Runnable {
        val registration = Registration(method, Regex(uriRegex), requestHandler)
        registrations.add(registration)
        return Runnable {
            registrations.remove(registration)
        }
    }

    override fun afterContextStartup() {
        bossGroup = NioEventLoopGroup(1)
        workerGroup = NioEventLoopGroup()

        val b = ServerBootstrap()
        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .handler(LoggingHandler(LogLevel.INFO))
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                public override fun initChannel(ch: SocketChannel) {
                    val pipeline = ch.pipeline()
                    pipeline.addLast(HttpServerCodec())
                    pipeline.addLast(HttpObjectAggregator(Short.MAX_VALUE.toInt()))
                    pipeline.addLast(HttpContentCompressor())
                    pipeline.addLast(
                        GempukkuHttpRequestHandler(
                            banChecker
                        ) { uri, request, remoteIp, responseWriter ->
                            pendingRequests.add(PendingRequest(uri, request, remoteIp, responseWriter))
                        })
                }
            })
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true)

        val bind = b.bind(port)
        serverChannel = bind.sync().channel()
    }

    override fun beforeContextStopped() {
        serverChannel?.close()?.sync()
        workerGroup?.shutdownGracefully()
        bossGroup?.shutdownGracefully()
    }

    override fun update() {
        synchronized(pendingRequests) {
            pendingRequests.forEach { request ->
                val registration = registrations.firstOrNull {
                    it.method == request.request.method && request.uri.matches(it.uriRegex)
                }
                try {
                    registration?.let {
                        registration.requestHandler.handleRequest(
                            request.uri,
                            request.request,
                            request.remoteIp,
                            request.responseWriter
                        )
                    } ?: run {
                        throw HttpProcessingException(404)
                    }
                } catch (exp: HttpProcessingException) {
                    request.responseWriter.writeError(exp.status, mapOf("message" to exp.message))
                } catch (exp: Exception) {
                    request.responseWriter.writeError(500)
                }
            }
            pendingRequests.clear()
        }
    }
}

private data class PendingRequest(
    val uri: String,
    val request: HttpRequest,
    val remoteIp: String,
    val responseWriter: ResponseWriter,
)

private data class Registration(
    val method: HttpMethod,
    val uriRegex: Regex,
    val requestHandler: ServerRequestHandler,
)