package com.gempukku.context.processor.inject

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectProperty(
    val value: String
)
