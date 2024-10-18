package com.gempukku.context.processor.inject

class FromAncestorsSingleInjectingSystem {
    @Inject(firstNotNullFromAncestors = true)
    lateinit var injectedSystem: InjectedSystem
}