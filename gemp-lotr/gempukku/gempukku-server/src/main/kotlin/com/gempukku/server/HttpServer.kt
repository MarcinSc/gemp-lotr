package com.gempukku.server

interface HttpServer {
    fun registerResponseHeadersProcessor(
        method: HttpMethod,
        uriRegex: String,
        responseHeaderProcessor: ServerResponseHeaderProcessor,
    ): Runnable

    fun registerRequestHandler(
        method: HttpMethod,
        uriRegex: String,
        requestHandler: ServerRequestHandler,
        validateOrigin: Boolean = true,
    ): Runnable

    fun generateSetCookieHeader(cookieName: String, cookieValue: String): Map<String, String>
}
