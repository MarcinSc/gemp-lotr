package com.gempukku.context.processor.inject

class MultipleInjectingSystem {
    @InjectList
    lateinit var injectedSystems: List<InjectedSystem>
}