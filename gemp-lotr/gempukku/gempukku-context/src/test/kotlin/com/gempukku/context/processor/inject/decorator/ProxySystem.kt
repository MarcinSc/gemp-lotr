package com.gempukku.context.processor.inject.decorator

import com.gempukku.context.resolver.expose.Exposes
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@Exposes(ProxyInterface::class)
class ProxySystem : ProxyInterface {
    @Volatile
    var executed: Boolean = false

    override fun execute() {
        executed = true
    }

    override fun executeWithResult(): String {
        return "Result"
    }

    override fun executeWithFuture(): Future<String> {
        return CompletableFuture.completedFuture("Result")
    }
}