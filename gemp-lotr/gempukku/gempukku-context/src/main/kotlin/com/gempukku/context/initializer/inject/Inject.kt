package com.gempukku.context.initializer.inject

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject(
    val allowsNull: Boolean = false,
    val firstNotNullFromAncestors: Boolean = true,
)
