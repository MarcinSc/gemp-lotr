package com.gempukku.context.processor.inject.decorator

interface SystemDecorator {
    fun <T> decorate(system: T, systemClass: Class<T>): T
}