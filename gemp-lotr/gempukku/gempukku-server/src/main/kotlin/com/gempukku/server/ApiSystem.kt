package com.gempukku.server

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes

@Exposes(LifecycleObserver::class)
abstract class ApiSystem: LifecycleObserver {
    private val deregistration: MutableList<Registration> = mutableListOf()

    @Inject
    protected lateinit var server: HttpServer

    override fun afterContextStartup() {
        deregistration.addAll(registerAPIs())
    }

    protected abstract fun registerAPIs(): List<Registration>

    override fun beforeContextStopped() {
        deregistration.forEach {
            it.deregister()
        }
        deregistration.clear()
    }
}