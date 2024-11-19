package com.gempukku.context.initializer.inject.priority

import com.gempukku.context.initializer.inject.PriorityPostfix
import com.gempukku.context.resolver.expose.Exposes

@PriorityPostfix("a")
@Exposes(DefaultPriorityInterface::class, NoDefaultPriorityInterface::class)
class APrioritySystem :
    DefaultPriorityInterface,
    NoDefaultPriorityInterface
