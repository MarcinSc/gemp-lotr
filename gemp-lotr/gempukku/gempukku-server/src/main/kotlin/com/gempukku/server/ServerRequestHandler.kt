package com.gempukku.server

fun interface ServerRequestHandler {
    fun handleRequest(uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter)
}