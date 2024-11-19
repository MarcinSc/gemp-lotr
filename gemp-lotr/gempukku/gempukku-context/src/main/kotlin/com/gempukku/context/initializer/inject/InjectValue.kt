package com.gempukku.context.initializer.inject

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectValue(
    val value: String,
)
