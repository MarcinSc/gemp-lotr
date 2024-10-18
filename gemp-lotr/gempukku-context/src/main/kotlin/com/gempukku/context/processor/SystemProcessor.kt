package com.gempukku.context.processor

import com.gempukku.context.GempukkuContext

/**
 * Processes all systems upon initialization.
 */
interface SystemProcessor {
    fun processSystems(context: GempukkuContext, vararg systems: Any)
}