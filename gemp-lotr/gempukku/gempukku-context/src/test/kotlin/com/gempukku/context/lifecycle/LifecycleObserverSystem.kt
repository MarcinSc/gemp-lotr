package com.gempukku.context.lifecycle

import com.gempukku.context.resolver.expose.Exposes

@Exposes(LifecycleObserver::class)
class LifecycleObserverSystem : LifecycleObserver {
    enum class State {
        STARTED, PAUSED, RESUMED, STOPPED
    }

    var state: State? = null

    override fun afterContextStartup() {
        state = State.STARTED
    }

    override fun beforeContextPaused() {
        state = State.PAUSED
    }

    override fun afterContextResumed() {
        state = State.RESUMED
    }

    override fun beforeContextStopped() {
        state = State.STOPPED
    }
}