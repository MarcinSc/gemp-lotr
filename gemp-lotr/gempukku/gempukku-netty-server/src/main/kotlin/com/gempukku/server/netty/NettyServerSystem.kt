package com.gempukku.server.netty

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.server.BanChecker
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServer
import com.gempukku.server.ResponseWriter
import com.gempukku.server.ServerRequestHandler
import com.gempukku.server.login.LoggedUserInterface
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpContentCompressor
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import java.util.Collections

@Exposes(LifecycleObserver::class, HttpServer::class, UpdatedSystem::class)
class NettyServerSystem :
    LifecycleObserver,
    HttpServer,
    UpdatedSystem {
    @InjectValue("server.netty.port")
    private var port: Int = 8080

    @InjectValue("server.netty.origin.pattern")
    private lateinit var originPattern: String

    @Inject(allowsNull = true)
    private var banChecker: BanChecker? = null

    @Inject(allowsNull = true)
    private var loggedUserInterface: LoggedUserInterface? = null

    private val originRegex by lazy {
        Regex(originPattern)
    }

    private val pendingRequests: MutableList<PendingRequest> = Collections.synchronizedList(mutableListOf())

    private val registrations: MutableList<Registration> = mutableListOf()

    private var bossGroup: NioEventLoopGroup? = null
    private var workerGroup: NioEventLoopGroup? = null
    private var serverChannel: Channel? = null

    override fun registerRequestHandler(
        method: HttpMethod,
        uriRegex: String,
        requestHandler: ServerRequestHandler,
        validateOrigin: Boolean,
    ): Runnable {
        val registration = Registration(method, Regex(uriRegex), requestHandler, validateOrigin)
        registrations.add(registration)
        return Runnable {
            registrations.remove(registration)
        }
    }

    override fun generateSetCookieHeader(
        cookieName: String,
        cookieValue: String,
    ): Map<String, String> =
        mapOf(
            HttpHeaderNames.SET_COOKIE.toString() to ServerCookieEncoder.STRICT.encode(cookieName, cookieValue),
        )

    val serverRequestHandler =
        ServerRequestHandler { uri, request, remoteIp, responseWriter ->
            pendingRequests.add(
                PendingRequest(
                    uri,
                    request,
                    remoteIp,
                    responseWriter,
                ),
            )
        }

    override fun afterContextStartup() {
        bossGroup = NioEventLoopGroup(1)
        workerGroup = NioEventLoopGroup()

        val b = ServerBootstrap()
        b
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .handler(LoggingHandler(LogLevel.INFO))
            .childHandler(
                object : ChannelInitializer<SocketChannel>() {
                    public override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        pipeline.addLast(HttpServerCodec())
                        pipeline.addLast(HttpObjectAggregator(Short.MAX_VALUE.toInt()))
                        pipeline.addLast(HttpContentCompressor())
                        pipeline.addLast(
                            GempukkuHttpRequestHandler(
                                banChecker,
                                loggedUserInterface,
                                serverRequestHandler,
                            ),
                        )
                    }
                },
            ).childOption(ChannelOption.SO_KEEPALIVE, true)
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
                try {
                    val registration =
                        registrations.firstOrNull {
                            it.method == request.request.method && request.uri.matches(it.uriRegex)
                        }
                    try {
                        registration?.let {
                            if (registration.validateOrigin) {
                                val origin = request.request.getHeader("Origin")
                                origin?.takeIf { !it.matches(originRegex) }?.run {
                                    throw HttpProcessingException(403)
                                }
                            }
                            registration.requestHandler.handleRequest(
                                request.uri,
                                request.request,
                                request.remoteIp,
                                request.responseWriter,
                            )
                        } ?: run {
                            throw HttpProcessingException(404)
                        }
                    } catch (exp: HttpProcessingException) {
                        request.responseWriter.writeError(exp.status, mapOf("message" to exp.message))
                    } catch (exp: Exception) {
                        request.responseWriter.writeError(500)
                    }
                } finally {
                    when (request.request) {
                        is NettyPostHttpRequest -> request.request.dispose()
                        is NettyGetHttpRequest -> request.request.dispose()
                    }
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
    val validateOrigin: Boolean,
)
