package com.gempukku.context.decorator

interface SystemDecorator {
    fun <T> decorate(system: T, systemClass: Class<T>): T
}
