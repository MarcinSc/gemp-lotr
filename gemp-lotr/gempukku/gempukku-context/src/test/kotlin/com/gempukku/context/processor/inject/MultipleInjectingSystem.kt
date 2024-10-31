package com.gempukku.context.processor.inject

class MultipleInjectingSystem {
    @InjectList(selectFromAncestors = false)
    lateinit var injectedSystems: List<InjectedSystem>
}
