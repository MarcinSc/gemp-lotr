package com.gempukku.context.resolver.expose

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Exposes(vararg val value: KClass<*>)
