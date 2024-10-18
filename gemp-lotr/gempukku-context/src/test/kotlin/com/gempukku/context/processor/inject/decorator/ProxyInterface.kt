package com.gempukku.context.processor.inject.decorator

import java.util.concurrent.Future

interface ProxyInterface {
    fun execute()
    fun executeWithResult(): String
    fun executeWithFuture(): Future<String>
}