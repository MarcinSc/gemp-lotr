package com.gempukku.ostream

interface ObjectStreamCustomizer<ConsumerType, FromType, ToType> {
    fun addConsumer(consumer: ConsumerType, resultStream: ObjectStream<ToType>)

    fun customizeCreated(id: String, value: FromType)

    fun customizeUpdated(id: String, value: FromType)

    fun customizeRemoved(id: String)

    fun removeConsumer(consumer: ConsumerType)
}
