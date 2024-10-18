package com.gempukku.context.lifecycle

interface LifecycleObserver {
    fun afterContextStartup() {

    }

    fun beforeContextPaused() {
        beforeContextStopped()
    }

    fun afterContextResumed() {
        afterContextStartup()
    }

    fun beforeContextStopped() {

    }
}