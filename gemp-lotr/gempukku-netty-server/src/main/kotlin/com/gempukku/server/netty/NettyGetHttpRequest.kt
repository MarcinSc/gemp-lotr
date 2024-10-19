package com.gempukku.server.netty

import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.codec.http.cookie.ServerCookieDecoder

class NettyGetHttpRequest(private val request: io.netty.handler.codec.http.HttpRequest) : HttpRequest {
    private val queryDecoder = QueryStringDecoder(request.uri())

    override val method: HttpMethod = HttpMethod.GET

    override fun getCookie(cookieName: String): String? {
        val cookieDecoder = ServerCookieDecoder.STRICT
        val cookieHeader = request.headers().get(HttpHeaderNames.COOKIE)
        cookieHeader?.let {
            val cookies = cookieDecoder.decode(cookieHeader)
            for (cookie in cookies) {
                if (cookie.name() == "loggedUser") {
                    return cookie.value()
                }
            }
        }
        return null
    }

    override fun getHeader(headerName: String): String? {
        return request.headers().get(headerName)
    }

    override fun getParameter(parameterName: String): String? {
        val parameterValues = queryDecoder.parameters()[parameterName]
        return parameterValues?.firstOrNull()
    }

    override fun getParameters(parameterName: String): List<String> {
        return queryDecoder.parameters()[parameterName].orEmpty()
    }

    fun dispose() {
    }
}