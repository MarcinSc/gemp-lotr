package com.gempukku.context.initializer.inject.priority

import com.gempukku.context.initializer.inject.PriorityPostfix
import com.gempukku.context.resolver.expose.Exposes

@PriorityPostfix("b")
@Exposes(DefaultPriorityInterface::class, NoDefaultPriorityInterface::class)
class BPrioritySystem :
    DefaultPriorityInterface,
    NoDefaultPriorityInterface
