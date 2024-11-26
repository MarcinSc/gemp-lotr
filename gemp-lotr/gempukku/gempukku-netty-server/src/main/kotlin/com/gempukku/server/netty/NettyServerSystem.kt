package com.gempukku.server.netty

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.server.BanChecker
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServer
import com.gempukku.server.ResponseWriter
import com.gempukku.server.ServerRequestHandler
import com.gempukku.server.ServerResponseHeaderProcessor
import com.gempukku.server.login.LoggedUserInterface
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.EmptyHttpHeaders
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpContentCompressor
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.util.CharsetUtil
import org.w3c.dom.Document
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.StringWriter
import java.net.InetSocketAddress
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.logging.Level
import java.util.logging.Logger
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

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

    companion object {
        private const val SIX_MONTHS = 1000L * 60L * 60L * 24L * 30L * 6L
        private val log: Logger = Logger.getLogger(GempukkuHttpRequestHandler::class.java.name)
        private val accessLog: Logger = Logger.getLogger("access")

        private fun send100Continue(ctx: ChannelHandlerContext) {
            val response: FullHttpResponse = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE)
            ctx.write(response)
            ctx.flush()
        }
    }

    private val pendingRequests: MutableList<PendingRequest> = Collections.synchronizedList(mutableListOf())

    private val requestHandlerRegistrations: MutableList<RequestHandlerRegistration> = mutableListOf()
    private val responseHeaderProcessorRegistration: MutableList<ResponseHeaderProcessorRegistration> = mutableListOf()

    private var bossGroup: NioEventLoopGroup? = null
    private var workerGroup: NioEventLoopGroup? = null
    private var serverChannel: Channel? = null

    override fun registerResponseHeadersProcessor(method: HttpMethod, uriRegex: String, responseHeaderProcessor: ServerResponseHeaderProcessor): Registration {
        val registration = ResponseHeaderProcessorRegistration(method, Regex(uriRegex), responseHeaderProcessor)
        responseHeaderProcessorRegistration.add(registration)
        return object : Registration {
            override fun deregister() {
                responseHeaderProcessorRegistration.remove(registration)
            }
        }
    }

    override fun registerRequestHandler(
        method: HttpMethod,
        uriRegex: String,
        requestHandler: ServerRequestHandler,
        validateOrigin: Boolean,
    ): Registration {
        val registration = RequestHandlerRegistration(method, Regex(uriRegex), requestHandler, validateOrigin)
        requestHandlerRegistrations.add(registration)
        return object : Registration {
            override fun deregister() {
                requestHandlerRegistrations.remove(registration)
            }
        }
    }

    override fun generateSetCookieHeader(cookieName: String, cookieValue: String): Map<String, String> =
        mapOf(
            HttpHeaderNames.SET_COOKIE.toString() to ServerCookieEncoder.STRICT.encode(cookieName, cookieValue),
        )

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
                            GempukkuHttpRequestHandler(),
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
        workerGroup?.shutdownGracefully()?.sync()
        bossGroup?.shutdownGracefully()?.sync()
    }

    override fun update() {
        synchronized(pendingRequests) {
            pendingRequests.forEach { pendingRequest ->
                val registration =
                    requestHandlerRegistrations.firstOrNull {
                        it.method == pendingRequest.request.method && pendingRequest.request.uri.matches(it.uriRegex)
                    }
                try {
                    registration?.let {
                        if (registration.validateOrigin) {
                            val origin = pendingRequest.request.getHeader("Origin")
                            origin?.takeIf { !it.matches(originRegex) }?.run {
                                throw HttpProcessingException(403)
                            }
                        }
                        registration.requestHandler.handleRequest(
                            pendingRequest.request,
                            pendingRequest.responseWriter,
                        )
                    } ?: run {
                        throw HttpProcessingException(404)
                    }
                } catch (exp: HttpProcessingException) {
                    pendingRequest.responseWriter.writeError(exp.status, mapOf("message" to exp.message))
                } catch (exp: Exception) {
                    pendingRequest.responseWriter.writeError(500)
                }
            }
            pendingRequests.clear()
        }
    }

    inner class RequestInformation(
        val request: HttpRequest,
        private val requestTime: Long,
    ) {
        fun printLog(statusCode: Int, finishedTime: Long) {
            if (accessLog.isLoggable(Level.FINE)) {
                accessLog.log(Level.FINE, request.remoteIp + "," + statusCode + "," + request.uri + "," + (finishedTime - requestTime))
            }
        }
    }

    inner class GempukkuHttpRequestHandler : SimpleChannelInboundHandler<FullHttpRequest>() {
        private val fileCache: MutableMap<String, ByteArray?> = Collections.synchronizedMap(HashMap())

        override fun channelRead0(ctx: ChannelHandlerContext, httpRequest: FullHttpRequest) {
            if (HttpUtil.is100ContinueExpected(httpRequest)) send100Continue(ctx)

            var uri = httpRequest.uri()

            if (uri.contains("?")) uri = uri.substring(0, uri.indexOf("?"))

            var ip = httpRequest.headers()["X-Forwarded-For"]

            if (ip == null) ip = (ctx.channel().remoteAddress() as InetSocketAddress).address.hostAddress

            val method = httpRequest.method().toInternal()

            val externalHttpRequest =
                when (method) {
                    HttpMethod.POST -> NettyPostHttpRequest(httpRequest, loggedUserInterface, uri, ip)
                    else -> NettyGetHttpRequest(httpRequest, loggedUserInterface, uri, ip)
                }

            val requestInformation =
                RequestInformation(
                    externalHttpRequest,
                    System.currentTimeMillis(),
                )

            val responseSender = ResponseSender(requestInformation, ctx, httpRequest)

            try {
                if (isBanned(requestInformation.request.remoteIp)) {
                    responseSender.writeError(401)
                    log.info("Denying entry to user from banned IP " + requestInformation.request.remoteIp)
                } else {
                    pendingRequests.add(
                        PendingRequest(
                            externalHttpRequest,
                            responseSender,
                        ),
                    )
                }
            } catch (exp: HttpProcessingException) {
                // 401, 403, 404, and other 400-series errors should just do minimal logging,
                when (val code = exp.status) {
                    400, in 501..599 -> {
                        log.log(
                            Level.SEVERE,
                            "HTTP code " + code + " response for " + requestInformation.request.remoteIp + ": " + requestInformation.request.uri,
                            exp,
                        )
                    }

                    in 401..499 -> {
                        log.log(
                            Level.FINE,
                            "HTTP " + code + " response for " + requestInformation.request.remoteIp + ": " + requestInformation.request.uri,
                        )
                    }
                }

                responseSender.writeError(exp.status, mapOf("message" to exp.message))
            } catch (exp: Exception) {
                log.log(Level.SEVERE, "Error response for $uri", exp)
                responseSender.writeError(500)
            }
        }

        private fun sendResponse(
            ctx: ChannelHandlerContext,
            request: io.netty.handler.codec.http.HttpRequest,
            response: FullHttpResponse,
            requestInformation: RequestInformation,
        ) {
            requestInformation.printLog(response.status().code(), System.currentTimeMillis())

            val keepAlive = HttpUtil.isKeepAlive(request)

            if (keepAlive) {
                // Add 'Content-Length' header only for a keep-alive connection.
                response.headers()[HttpHeaderNames.CONTENT_LENGTH] = response.content().readableBytes()
                // Add keep alive header as per:
                // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
                response.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.KEEP_ALIVE
            }

            ctx.write(response)
            ctx.flush()

            if (!keepAlive) {
                // If keep-alive is off, close the connection once the content is fully written.
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
            }
        }

        private fun isBanned(ipAddress: String): Boolean = banChecker?.isBanned(ipAddress) ?: false

        private fun getHeadersForFile(headers: Map<String, String>?, file: File): Map<String, String> {
            val fileHeaders: MutableMap<String, String> = HashMap(headers ?: emptyMap())

            val disableCaching = false
            var cache = false

            val fileName = file.name
            val contentType: String
            if (fileName.endsWith(".html")) {
                contentType = "text/html; charset=UTF-8"
            } else if (fileName.endsWith(".js")) {
                contentType = "application/javascript; charset=UTF-8"
            } else if (fileName.endsWith(".css")) {
                contentType = "text/css; charset=UTF-8"
            } else if (fileName.endsWith(".jpg")) {
                cache = true
                contentType = "image/jpeg"
            } else if (fileName.endsWith(".png")) {
                cache = true
                contentType = "image/png"
            } else if (fileName.endsWith(".gif")) {
                cache = true
                contentType = "image/gif"
            } else if (fileName.endsWith(".svg")) {
                cache = true
                contentType = "image/svg+xml"
            } else if (fileName.endsWith(".wav")) {
                cache = true
                contentType = "audio/wav"
            } else {
                contentType = "application/octet-stream"
            }

            if (disableCaching) {
                fileHeaders[HttpHeaderNames.CACHE_CONTROL.toString()] = "no-cache"
                fileHeaders[HttpHeaderNames.PRAGMA.toString()] = "no-cache"
                fileHeaders[HttpHeaderNames.EXPIRES.toString()] = (-1).toString()
            } else if (cache) {
                val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
                val sixMonthsFromNow = System.currentTimeMillis() + SIX_MONTHS
                fileHeaders[HttpHeaderNames.EXPIRES.toString()] = dateFormat.format(Date(sixMonthsFromNow))
            }

            fileHeaders[HttpHeaderNames.CONTENT_TYPE.toString()] = contentType
            return fileHeaders
        }

        private fun convertToHeaders(headersMap: Map<String, String>?): HttpHeaders {
            val headers: HttpHeaders = DefaultHttpHeaders()
            if (headersMap != null) {
                for ((key, value) in headersMap) {
                    headers[key] = value
                }
            }
            return headers
        }

        @Deprecated("Deprecated in Java")
        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            if (cause !is IOException && cause !is IllegalArgumentException) {
                log.log(Level.SEVERE, "Error while processing request", cause)
            }
            ctx.close()
        }

        private inner class ResponseSender(
            private val requestInformation: RequestInformation,
            private val ctx: ChannelHandlerContext,
            private val request: io.netty.handler.codec.http.HttpRequest,
        ) : ResponseWriter {
            override fun writeError(status: Int, headersMap: Map<String, String>?) {
                val content = ByteArray(0)
                // Build the response object.
                val response: FullHttpResponse =
                    DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.valueOf(status),
                        Unpooled.wrappedBuffer(content),
                        convertToHeaders(headersMap),
                        EmptyHttpHeaders.INSTANCE,
                    )
                writeResponse(ctx, request, response, requestInformation)
            }

            override fun writeXmlResponse(document: Document?, headersMap: Map<String, String>?) {
                try {
                    val textResponse =
                        if (document != null) {
                            val domSource = DOMSource(document)
                            val writer = StringWriter()
                            val result = StreamResult(writer)
                            val tf = TransformerFactory.newInstance()
                            val transformer = tf.newTransformer()
                            transformer.transform(domSource, result)

                            writer.toString()
                        } else {
                            "<result>OK</result>"
                        }

                    val contentType = "application/xml; charset=UTF-8"

                    val headers1 = convertToHeaders(headersMap)
                    headers1[HttpHeaderNames.CONTENT_TYPE] = contentType

                    // Build the response object.
                    val response: FullHttpResponse =
                        DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK,
                            Unpooled.wrappedBuffer(textResponse.toByteArray(CharsetUtil.UTF_8)),
                            headers1,
                            EmptyHttpHeaders.INSTANCE,
                        )
                    writeResponse(ctx, request, response, requestInformation)
                } catch (exp: Exception) {
                    val content = ByteArray(0)
                    // Build the response object.
                    log.log(Level.SEVERE, "Error response for " + request.uri(), exp)
                    val response: FullHttpResponse =
                        DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.INTERNAL_SERVER_ERROR,
                            Unpooled.wrappedBuffer(content),
                            null,
                            EmptyHttpHeaders.INSTANCE,
                        )
                    writeResponse(ctx, request, response, requestInformation)
                }
            }

            override fun writeHtmlResponse(html: String, headersMap: Map<String, String>?) {
                val headers: HttpHeaders = convertToHeaders(headersMap)
                headers[HttpHeaderNames.CONTENT_TYPE] = "text/html; charset=UTF-8"

                // Build the response object.
                val response: FullHttpResponse =
                    DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.wrappedBuffer(html.toByteArray(CharsetUtil.UTF_8)),
                        headers,
                        EmptyHttpHeaders.INSTANCE,
                    )
                writeResponse(ctx, request, response, requestInformation)
            }

            override fun writeJsonResponse(json: String, headersMap: Map<String, String>?) {
                val headers: HttpHeaders = convertToHeaders(headersMap)
                headers[HttpHeaderNames.CONTENT_TYPE] = "application/json; charset=UTF-8"

                // Build the response object.
                val response: FullHttpResponse =
                    DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.wrappedBuffer(json.toByteArray(CharsetUtil.UTF_8)),
                        headers,
                        EmptyHttpHeaders.INSTANCE,
                    )
                writeResponse(ctx, request, response, requestInformation)
            }

            override fun writeByteResponse(bytes: ByteArray, headersMap: Map<String, String>?) {
                val headers1 = convertToHeaders(headersMap)

                // Build the response object.
                val response: FullHttpResponse =
                    DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.wrappedBuffer(bytes),
                        headers1,
                        EmptyHttpHeaders.INSTANCE,
                    )
                writeResponse(ctx, request, response, requestInformation)
            }

            override fun writeFile(file: File, headersMap: Map<String, String>?) {
                try {
                    val canonicalPath = file.canonicalPath
                    var fileBytes = fileCache[canonicalPath]
                    if (fileBytes == null) {
                        if (!file.exists() || !file.isFile) {
                            val content = ByteArray(0)
                            // Build the response object.
                            val response: FullHttpResponse =
                                DefaultFullHttpResponse(
                                    HttpVersion.HTTP_1_1,
                                    HttpResponseStatus.valueOf(404),
                                    Unpooled.wrappedBuffer(content),
                                    convertToHeaders(null),
                                    EmptyHttpHeaders.INSTANCE,
                                )
                            writeResponse(ctx, request, response, requestInformation)
                            return
                        }

                        val fis = FileInputStream(file)
                        try {
                            val baos = ByteArrayOutputStream()
                            copyLarge(fis, baos)
                            fileBytes = baos.toByteArray()
                            fileCache[canonicalPath] = fileBytes
                        } finally {
                            try {
                                fis.close()
                            } catch (e: Exception) {
                                // Ignored
                            }
                        }
                    }

                    val headers1 = convertToHeaders(getHeadersForFile(headersMap, file))

                    // Build the response object.
                    val response: FullHttpResponse =
                        DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK,
                            Unpooled.wrappedBuffer(fileBytes),
                            headers1,
                            EmptyHttpHeaders.INSTANCE,
                        )
                    writeResponse(ctx, request, response, requestInformation)
                } catch (exp: IOException) {
                    val content = ByteArray(0)
                    // Build the response object.
                    log.log(Level.SEVERE, "Error response for " + request.uri(), exp)
                    val response: FullHttpResponse =
                        DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.valueOf(500),
                            Unpooled.wrappedBuffer(content),
                            convertToHeaders(null),
                            EmptyHttpHeaders.INSTANCE,
                        )
                    writeResponse(ctx, request, response, requestInformation)
                }
            }

            private fun writeResponse(
                ctx: ChannelHandlerContext,
                request: io.netty.handler.codec.http.HttpRequest,
                response: FullHttpResponse,
                requestInformation: RequestInformation,
            ) {
                responseHeaderProcessorRegistration.forEach {
                    if (requestInformation.request.method == it.method && requestInformation.request.uri.matches(it.uriRegex)) {
                        it.responseHeaderProcessor.getExtraHeaders(requestInformation.request).forEach { (key, value) ->
                            response.headers()[key] = value
                        }
                    }
                }

                when (requestInformation.request) {
                    is NettyPostHttpRequest -> requestInformation.request.dispose()
                    is NettyGetHttpRequest -> requestInformation.request.dispose()
                }

                sendResponse(ctx, request, response, requestInformation)
            }

            private fun copyLarge(inputStream: InputStream, outputStream: OutputStream, buffer: ByteArray = ByteArray(8192)): Long {
                var count = 0L
                var n: Int
                while (-1 != (inputStream.read(buffer).also { n = it })) {
                    outputStream.write(buffer, 0, n)
                    count += n.toLong()
                }

                return count
            }
        }
    }

    private fun io.netty.handler.codec.http.HttpMethod.toInternal(): HttpMethod =
        when (this) {
            io.netty.handler.codec.http.HttpMethod.POST -> HttpMethod.POST
            else -> HttpMethod.GET
        }
}

private data class PendingRequest(
    val request: HttpRequest,
    val responseWriter: ResponseWriter,
)

private data class RequestHandlerRegistration(
    val method: HttpMethod,
    val uriRegex: Regex,
    val requestHandler: ServerRequestHandler,
    val validateOrigin: Boolean,
)

private data class ResponseHeaderProcessorRegistration(
    val method: HttpMethod,
    val uriRegex: Regex,
    val responseHeaderProcessor: ServerResponseHeaderProcessor,
)
