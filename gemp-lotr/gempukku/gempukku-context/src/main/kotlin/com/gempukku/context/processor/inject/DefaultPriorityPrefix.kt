package com.gempukku.context.processor.inject

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultPriorityPrefix(
    val value: String
)
