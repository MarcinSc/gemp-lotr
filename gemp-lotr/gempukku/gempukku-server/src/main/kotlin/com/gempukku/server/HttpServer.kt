package com.gempukku.server

import com.gempukku.context.Registration

interface HttpServer {
    fun registerResponseHeadersProcessor(method: HttpMethod, uriRegex: String, responseHeaderProcessor: ServerResponseHeaderProcessor): Registration

    fun registerRequestHandler(
        method: HttpMethod,
        uriRegex: String,
        requestHandler: ServerRequestHandler,
        validateOrigin: Boolean = true,
    ): Registration

    fun generateSetCookieHeader(cookieName: String, cookieValue: String): Map<String, String>
}
