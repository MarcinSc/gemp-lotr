package com.gempukku.context.processor.inject

class FromAncestorsMultipleInjectingSystem {
    @InjectList(selectFromAncestors = true)
    lateinit var injectedSystems: List<InjectedSystem>
}