package com.gempukku.context.lifecycle

import com.gempukku.context.processor.inject.InjectList

class LifecycleSystem {
    @InjectList(priorityPrefix = "lifecycle.start")
    private lateinit var startObservers: List<LifecycleObserver>

    @InjectList(priorityPrefix = "lifecycle.pause")
    private lateinit var pauseObservers: List<LifecycleObserver>

    @InjectList(priorityPrefix = "lifecycle.resume")
    private lateinit var resumeObservers: List<LifecycleObserver>

    @InjectList(priorityPrefix = "lifecycle.stop")
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