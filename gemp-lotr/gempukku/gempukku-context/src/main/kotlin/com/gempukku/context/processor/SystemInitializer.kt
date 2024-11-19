package com.gempukku.context.processor

import com.gempukku.context.GempukkuContext

/**
 * Processes all systems upon initialization.
 */
interface SystemInitializer {
    fun processSystems(context: GempukkuContext, systems: Collection<Any>)
}
