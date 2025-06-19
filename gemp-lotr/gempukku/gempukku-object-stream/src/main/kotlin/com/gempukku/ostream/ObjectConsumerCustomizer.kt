package com.gempukku.ostream

interface ObjectConsumerCustomizer<ConsumerType, FromType, ToType> {
    fun customize(consumer: ConsumerType, id: String, value: FromType): ToType
}
