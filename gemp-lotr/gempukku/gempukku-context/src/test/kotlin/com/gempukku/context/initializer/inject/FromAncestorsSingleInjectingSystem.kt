package com.gempukku.context.initializer.inject

class FromAncestorsSingleInjectingSystem {
    @Inject(firstNotNullFromAncestors = true)
    lateinit var injectedSystem: InjectedSystem
}
