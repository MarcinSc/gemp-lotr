package com.gempukku.context.initializer.inject

class NullableSingleInjectingSystem {
    @Inject(allowsNull = true)
    val injectedSystem: InjectedSystem? = null
}
