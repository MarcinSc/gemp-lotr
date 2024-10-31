package com.gempukku.context.processor.inject

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectValue(
    val value: String,
)
