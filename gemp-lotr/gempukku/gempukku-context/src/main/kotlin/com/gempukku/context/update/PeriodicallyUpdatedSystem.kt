package com.gempukku.context.update

abstract class PeriodicallyUpdatedSystem(private val skip: Int) : UpdatedSystem {
    private var count: Int = 0
    override fun update() {
        count++
        count %= skip
        if (count == 0) {
            periodicUpdate()
        }
    }

    abstract fun periodicUpdate()
}