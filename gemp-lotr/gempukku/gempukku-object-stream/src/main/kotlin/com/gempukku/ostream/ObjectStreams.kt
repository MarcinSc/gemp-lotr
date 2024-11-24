package com.gempukku.ostream

fun <ConsumerType, FromType, ToType> ObjectConsumerCustomizer<ConsumerType, FromType, ToType>.persistentState(): ObjectStreamCustomizer<ConsumerType, FromType, ToType> {
    return PersistentStateCustomizer(this)
}

fun <ConsumerType, FromType, ToType> ObjectStreamCustomizer<ConsumerType, FromType, ToType>.visibilityCheck(visibilityCheck: VisibilityCheck<ConsumerType, ToType>): ObjectStreamCustomizer<ConsumerType, FromType, ToType> {
    return VisibilityCustomizer(this, visibilityCheck)
}

fun <ConsumerType, FromType, ToType> ObjectStreamCustomizer<ConsumerType, FromType, ToType>.deduplidateUpdates(): ObjectStreamCustomizer<ConsumerType, FromType, ToType> {
    return DeduplicateUpdatesCustomizer(this)
}
