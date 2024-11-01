package com.gempukku.server

interface HttpServer {
    fun registerRequestHandler(
        method: HttpMethod,
        uriRegex: String,
        requestHandler: ServerRequestHandler,
        validateOrigin: Boolean = true,
    ): Runnable

    fun generateSetCookieHeader(cookieName: String, cookieValue: String): Map<String, String>
}
