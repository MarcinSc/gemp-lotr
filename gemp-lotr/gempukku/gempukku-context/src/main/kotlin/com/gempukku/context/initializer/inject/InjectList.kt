package com.gempukku.context.initializer.inject

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectList(
    val selectFromAncestors: Boolean = true,
    val priorityPrefix: String = "",
)
