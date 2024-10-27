package com.gempukku.context.processor.inject

class NullableSingleInjectingSystem {
    @Inject(allowsNull = true)
    val injectedSystem: InjectedSystem? = null
}