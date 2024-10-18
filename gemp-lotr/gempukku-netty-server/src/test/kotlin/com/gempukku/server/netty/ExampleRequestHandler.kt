package com.gempukku.server.netty

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServerSystem
import com.gempukku.server.ResponseWriter
import com.gempukku.server.ServerRequestHandler

@Exposes(LifecycleObserver::class)
class ExampleRequestHandler : ServerRequestHandler, LifecycleObserver {
    @Inject
    private lateinit var serverSystem: HttpServerSystem

    override fun handleRequest(uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) {
        responseWriter.writeHtmlResponse("<html><body>Hello world!</body></html>")
    }

    override fun afterContextStartup() {
        serverSystem.registerRequestHandler(HttpMethod.GET, "^/example$", this)
    }
}