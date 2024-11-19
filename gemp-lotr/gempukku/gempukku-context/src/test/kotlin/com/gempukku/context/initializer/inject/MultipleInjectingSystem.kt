package com.gempukku.context.initializer.inject

class MultipleInjectingSystem {
    @InjectList(selectFromAncestors = false)
    lateinit var injectedSystems: List<InjectedSystem>
}
