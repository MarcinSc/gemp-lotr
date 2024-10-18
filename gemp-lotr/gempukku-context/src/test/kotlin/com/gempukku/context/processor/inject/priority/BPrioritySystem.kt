package com.gempukku.context.processor.inject.priority

import com.gempukku.context.processor.inject.PriorityPostfix
import com.gempukku.context.resolver.expose.Exposes

@PriorityPostfix("b")
@Exposes(DefaultPriorityInterface::class, NoDefaultPriorityInterface::class)
class BPrioritySystem : DefaultPriorityInterface, NoDefaultPriorityInterface {
}