package com.gempukku.server

interface HttpServerSystem {
    fun registerRequestHandler(method: HttpMethod, uriRegex: String, requestHandler: ServerRequestHandler): Runnable

    fun generateSetCookieHeader(cookieName: String, cookieValue: String): Map<String, String>
}