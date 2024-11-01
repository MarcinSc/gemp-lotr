package com.gempukku.server.netty

import com.gempukku.server.BanChecker
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.ResponseWriter
import com.gempukku.server.ServerRequestHandler
import com.gempukku.server.login.LoggedUserInterface
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.EmptyHttpHeaders
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion
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
import kotlin.collections.HashMap

class GempukkuHttpRequestHandler(
    private val banChecker: BanChecker?,
    private val loggedUserInterface: LoggedUserInterface?,
    private val requestHandler: ServerRequestHandler,
) : SimpleChannelInboundHandler<FullHttpRequest>() {
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

    private val fileCache: MutableMap<String, ByteArray?> = Collections.synchronizedMap(HashMap())

    private class RequestInformation(
        val uri: String,
        val remoteIp: String,
        private val requestTime: Long,
    ) {
        fun printLog(statusCode: Int, finishedTime: Long) {
            if (accessLog.isLoggable(Level.FINE)) {
                accessLog.log(Level.FINE, remoteIp + "," + statusCode + "," + uri + "," + (finishedTime - requestTime))
            }
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext, httpRequest: FullHttpRequest) {
        if (HttpUtil.is100ContinueExpected(httpRequest)) send100Continue(ctx)

        var uri = httpRequest.uri()

        if (uri.contains("?")) uri = uri.substring(0, uri.indexOf("?"))

        var ip = httpRequest.headers()["X-Forwarded-For"]

        if (ip == null) ip = (ctx.channel().remoteAddress() as InetSocketAddress).address.hostAddress

        val requestInformation =
            RequestInformation(
                httpRequest.uri(),
                ip,
                System.currentTimeMillis(),
            )

        val responseSender = ResponseSender(requestInformation, ctx, httpRequest)

        try {
            if (isBanned(requestInformation.remoteIp)) {
                responseSender.writeError(401)
                log.info("Denying entry to user from banned IP " + requestInformation.remoteIp)
            } else {
                val externalHttpRequest =
                    when (httpRequest.method().toInternal()) {
                        HttpMethod.POST -> NettyPostHttpRequest(httpRequest, loggedUserInterface)
                        else -> NettyGetHttpRequest(httpRequest, loggedUserInterface)
                    }

                requestHandler.handleRequest(uri, externalHttpRequest, requestInformation.remoteIp, responseSender)
            }
        } catch (exp: HttpProcessingException) {
            // 401, 403, 404, and other 400-series errors should just do minimal logging,
            when (val code = exp.status) {
                400, in 501..599 -> {
                    log.log(
                        Level.SEVERE,
                        "HTTP code " + code + " response for " + requestInformation.remoteIp + ": " + requestInformation.uri,
                        exp,
                    )
                }
                in 401..499 -> {
                    log.log(
                        Level.FINE,
                        "HTTP " + code + " response for " + requestInformation.remoteIp + ": " + requestInformation.uri,
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
        request: HttpRequest,
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
        private val request: HttpRequest,
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
            sendResponse(ctx, request, response, requestInformation)
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
                sendResponse(ctx, request, response, requestInformation)
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
                sendResponse(ctx, request, response, requestInformation)
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
            sendResponse(ctx, request, response, requestInformation)
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
            sendResponse(ctx, request, response, requestInformation)
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
            sendResponse(ctx, request, response, requestInformation)
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
                        sendResponse(ctx, request, response, requestInformation)
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
                sendResponse(ctx, request, response, requestInformation)
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
                sendResponse(ctx, request, response, requestInformation)
            }
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