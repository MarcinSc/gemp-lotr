package com.gempukku.context.initializer.inject.decorator

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

    override fun executeWithResult(): String = "Result"

    override fun executeWithFuture(): Future<String> = CompletableFuture.completedFuture("Result")
}