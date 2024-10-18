package com.gempukku.context.processor.inject

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PriorityPostfix(
    val value: String
)
