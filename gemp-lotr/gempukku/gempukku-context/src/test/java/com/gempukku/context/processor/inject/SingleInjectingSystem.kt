package com.gempukku.context.processor.inject

class SingleInjectingSystem {
    @Inject(firstNotNullFromAncestors = false)
    lateinit var injectedSystem: InjectedSystem
}
