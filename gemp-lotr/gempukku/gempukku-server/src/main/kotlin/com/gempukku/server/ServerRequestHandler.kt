package com.gempukku.server

fun interface ServerRequestHandler {
    fun handleRequest(
        request: HttpRequest,
        responseWriter: ResponseWriter,
    )
}
