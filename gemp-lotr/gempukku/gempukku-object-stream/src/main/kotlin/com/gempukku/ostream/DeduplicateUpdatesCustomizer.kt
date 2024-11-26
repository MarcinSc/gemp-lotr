package com.gempukku.ostream

class DeduplicateUpdatesCustomizer<ConsumerType, FromType, ToType>(
    private val delegate: ObjectStreamCustomizer<ConsumerType, FromType, ToType>,
) : ObjectStreamCustomizer<ConsumerType, FromType, ToType> {
    override fun addConsumer(consumer: ConsumerType, resultStream: ObjectStream<ToType>) {
        delegate.addConsumer(consumer, InterceptingObjectStream(resultStream))
    }

    override fun customizeCreated(id: String, value: FromType) {
        delegate.customizeCreated(id, value)
    }

    override fun customizeUpdated(id: String, value: FromType) {
        delegate.customizeUpdated(id, value)
    }

    override fun customizeRemoved(id: String) {
        delegate.customizeRemoved(id)
    }

    override fun removeConsumer(consumer: ConsumerType) {
        delegate.removeConsumer(consumer)
    }

    inner class InterceptingObjectStream<ToType>(
        private val delegate: ObjectStream<ToType>,
    ) : ObjectStream<ToType> {
        private val stateMap = mutableMapOf<String, ToType>()

        override fun objectCreated(id: String, value: ToType) {
            stateMap[id] = value
            delegate.objectCreated(id, value)
        }

        override fun objectUpdated(id: String, value: ToType) {
            if (stateMap[id] == value) return
            stateMap[id] = value
            delegate.objectUpdated(id, value)
        }

        override fun objectRemoved(id: String) {
            stateMap.remove(id)
            delegate.objectRemoved(id)
        }
    }
}
