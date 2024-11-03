package com.gempukku.context.update

import com.gempukku.context.ContextScheduledExecutor
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectList
import com.gempukku.context.processor.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import java.util.concurrent.TimeUnit

@Exposes(LifecycleObserver::class)
class UpdatingSystem : LifecycleObserver {
    @InjectValue("update.frequency")
    private var frequency: Long = 100

    @Inject
    private lateinit var executor: ContextScheduledExecutor

    @InjectList(priorityPrefix = "updating", selectFromAncestors = false)
    private lateinit var updatedSystems: List<UpdatedSystem>

    private var scheduledTaskTurnOff: Runnable? = null

    override fun afterContextStartup() {
        scheduledTaskTurnOff =
            executor.scheduleAtFixedRate({
                updatedSystems.forEach { it.update() }
            }, frequency, frequency, TimeUnit.MILLISECONDS)
    }

    override fun beforeContextStopped() {
        scheduledTaskTurnOff?.run()
    }
}
