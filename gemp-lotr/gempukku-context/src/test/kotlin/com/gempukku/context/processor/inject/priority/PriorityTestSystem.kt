package com.gempukku.context.processor.inject.priority

import com.gempukku.context.processor.inject.InjectList

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