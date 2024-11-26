package com.gempukku.ostream

class PersistentStateCustomizer<ConsumerType, FromType, ToType>(
    private val delegate: ObjectConsumerCustomizer<ConsumerType, FromType, ToType>,
) : ObjectStreamCustomizer<ConsumerType, FromType, ToType> {
    private val consumers: MutableMap<ConsumerType, ObjectStream<ToType>> = mutableMapOf()
    private val maintainedObjects: MutableMap<String, FromType> = mutableMapOf()

    override fun addConsumer(consumer: ConsumerType, resultStream: ObjectStream<ToType>) {
        consumers[consumer] = resultStream
        maintainedObjects.forEach {
            val result = delegate.customize(consumer, it.key, it.value)
            resultStream.objectCreated(it.key, result)
        }
    }

    override fun customizeCreated(id: String, value: FromType) {
        maintainedObjects[id] = value
        consumers.forEach {
            val result = delegate.customize(it.key, id, value)
            it.value.objectCreated(id, result)
        }
    }

    override fun customizeUpdated(id: String, value: FromType) {
        consumers.forEach {
            val result = delegate.customize(it.key, id, value)
            it.value.objectUpdated(id, result)
        }
    }

    override fun customizeRemoved(id: String) {
        maintainedObjects.remove(id)
        consumers.forEach {
            it.value.objectRemoved(id)
        }
    }

    override fun removeConsumer(consumer: ConsumerType) {
        maintainedObjects.forEach {
            val resultStream = consumers[consumer] ?: throw IllegalStateException("Consumer not found")
            resultStream.objectRemoved(it.key)
        }
        consumers.remove(consumer)
    }
}
