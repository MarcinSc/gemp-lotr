package com.gempukku.server.netty

import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpRequest
import com.gempukku.server.login.LoggedUser
import com.gempukku.server.login.LoggedUserInterface
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.multipart.Attribute
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import io.netty.handler.codec.http.multipart.InterfaceHttpData
import java.util.LinkedList

class NettyPostHttpRequest(
    private val request: io.netty.handler.codec.http.HttpRequest,
    private val loggedUserInterface: LoggedUserInterface?,
    override val uri: String,
    override val remoteIp: String,
) : HttpRequest {
    private val requestDecoder = HttpPostRequestDecoder(request)

    override val method: HttpMethod = HttpMethod.POST

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

    override fun getHeader(headerName: String): String? = request.headers().get(headerName)

    override fun getParameter(parameterName: String): String? {
        val data = requestDecoder.getBodyHttpData(parameterName) ?: return null
        if (data.httpDataType == InterfaceHttpData.HttpDataType.Attribute) {
            val attribute = data as Attribute
            return attribute.value
        } else {
            return null
        }
    }

    override fun getParameters(parameterName: String): List<String> {
        val datas = requestDecoder.getBodyHttpDatas(parameterName) ?: return emptyList()
        val result: MutableList<String> = LinkedList()
        for (data in datas) {
            if (data.httpDataType == InterfaceHttpData.HttpDataType.Attribute) {
                val attribute = data as Attribute
                result.add(attribute.value)
            }
        }
        return result
    }

    override fun getLoggedUserId(): LoggedUser? = loggedUserInterface?.findLoggedUser(this)

    fun dispose() {
        requestDecoder.destroy()
    }
}
