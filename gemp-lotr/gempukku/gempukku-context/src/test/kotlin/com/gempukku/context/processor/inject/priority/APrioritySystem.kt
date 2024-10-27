package com.gempukku.context.processor.inject.priority

import com.gempukku.context.processor.inject.PriorityPostfix
import com.gempukku.context.resolver.expose.Exposes

@PriorityPostfix("a")
@Exposes(DefaultPriorityInterface::class, NoDefaultPriorityInterface::class)
class APrioritySystem : DefaultPriorityInterface, NoDefaultPriorityInterface {
}