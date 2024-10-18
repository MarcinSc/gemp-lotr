package com.gempukku.context.update

import com.gempukku.context.resolver.expose.Exposes

@Exposes(UpdatedSystem::class)
class PeriodicallyUpdatedTestSystem : PeriodicallyUpdatedSystem(10) {
    var invocationCount: Int = 0

    override fun periodicUpdate() {
        invocationCount++
    }
}