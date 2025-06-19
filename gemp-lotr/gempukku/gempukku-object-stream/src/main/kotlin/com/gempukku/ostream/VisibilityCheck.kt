package com.gempukku.ostream

interface VisibilityCheck<ConsumerType, ToType> {
    fun checkVisibility(consumer: ConsumerType, to: ToType): Boolean
}
