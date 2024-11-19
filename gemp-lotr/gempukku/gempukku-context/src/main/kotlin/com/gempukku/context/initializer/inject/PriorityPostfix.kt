package com.gempukku.context.initializer.inject

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PriorityPostfix(
    val value: String,
)
