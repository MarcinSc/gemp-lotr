package com.gempukku.context.initializer.inject

class FromAncestorsMultipleInjectingSystem {
    @InjectList(selectFromAncestors = true)
    lateinit var injectedSystems: List<InjectedSystem>
}
