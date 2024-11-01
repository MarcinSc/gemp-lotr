package com.gempukku.context.processor.inject.decorator

import com.gempukku.context.processor.inject.Inject
import java.util.concurrent.Future

class CallingSystem {
    @Inject
    private lateinit var proxy: ProxyInterface

    fun execute() {
        proxy.execute()
    }

    fun executeWithResult(): String = proxy.executeWithResult()

    fun executeWithFuture(): Future<String> = proxy.executeWithFuture()
}