package com.gempukku.server.netty

import com.gempukku.server.ResponseWriter
import io.netty.handler.codec.http.HttpRequest

interface NettyServerRequestHandler {
    fun handleRequest(
        uri: String,
        request: HttpRequest,
        remoteIp: String,
        responseWriter: ResponseWriter,
    )
}
