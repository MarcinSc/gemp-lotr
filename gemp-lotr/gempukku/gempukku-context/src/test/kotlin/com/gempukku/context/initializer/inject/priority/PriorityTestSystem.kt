package com.gempukku.context.initializer.inject.priority

import com.gempukku.context.initializer.inject.InjectList

class PriorityTestSystem {
    @InjectList
    lateinit var noDefaultNoPriority: List<NoDefaultPriorityInterface>

    @InjectList(priorityPrefix = "prefix")
    lateinit var noDefaultWithPriority: List<NoDefaultPriorityInterface>

    @InjectList
    lateinit var defaultNoPriority: List<DefaultPriorityInterface>

    @InjectList(priorityPrefix = "override")
    lateinit var defaultWithPriority: List<DefaultPriorityInterface>
}
