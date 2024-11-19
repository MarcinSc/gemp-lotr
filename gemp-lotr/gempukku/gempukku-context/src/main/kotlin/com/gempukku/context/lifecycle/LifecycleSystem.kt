package com.gempukku.context.lifecycle

import com.gempukku.context.initializer.inject.InjectList

class LifecycleSystem {
    @InjectList(priorityPrefix = "lifecycle.start", selectFromAncestors = false)
    private lateinit var startObservers: List<LifecycleObserver>

    @InjectList(priorityPrefix = "lifecycle.pause", selectFromAncestors = false)
    private lateinit var pauseObservers: List<LifecycleObserver>

    @InjectList(priorityPrefix = "lifecycle.resume", selectFromAncestors = false)
    private lateinit var resumeObservers: List<LifecycleObserver>

    @InjectList(priorityPrefix = "lifecycle.stop", selectFromAncestors = false)
    private lateinit var stopObservers: List<LifecycleObserver>

    fun start() {
        startObservers.forEach { observer -> observer.afterContextStartup() }
    }

    fun pause() {
        pauseObservers.forEach { observer -> observer.beforeContextPaused() }
    }

    fun resume() {
        resumeObservers.forEach { observer -> observer.afterContextResumed() }
    }

    fun stop() {
        stopObservers.forEach { observer -> observer.beforeContextStopped() }
    }
}
