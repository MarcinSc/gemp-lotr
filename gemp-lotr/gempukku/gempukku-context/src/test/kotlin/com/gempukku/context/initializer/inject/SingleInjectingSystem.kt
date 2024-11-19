package com.gempukku.context.initializer.inject

class SingleInjectingSystem {
    @Inject(firstNotNullFromAncestors = false)
    lateinit var injectedSystem: InjectedSystem
}
