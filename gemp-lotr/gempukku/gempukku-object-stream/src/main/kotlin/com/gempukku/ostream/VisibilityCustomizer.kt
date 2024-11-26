package com.gempukku.ostream

class VisibilityCustomizer<ConsumerType, FromType, ToType>(
    private val delegate: ObjectStreamCustomizer<ConsumerType, FromType, ToType>,
    private val visibilityCheck: VisibilityCheck<ConsumerType, ToType>,
) : ObjectStreamCustomizer<ConsumerType, FromType, ToType> {
    override fun addConsumer(consumer: ConsumerType, resultStream: ObjectStream<ToType>) {
        delegate.addConsumer(consumer, InterceptingObjectStream(consumer, resultStream))
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

    private inner class InterceptingObjectStream(
        private val consumer: ConsumerType,
        private val delegate: ObjectStream<ToType>,
    ) : ObjectStream<ToType> {
        private val hasObject: MutableSet<String> = mutableSetOf()

        override fun objectCreated(id: String, value: ToType) {
            if (visibilityCheck.checkVisibility(consumer, value)) {
                create(id, value)
            }
        }

        override fun objectUpdated(id: String, value: ToType) {
            if (hasObject.contains(id)) {
                if (visibilityCheck.checkVisibility(consumer, value)) {
                    update(id, value)
                } else {
                    remove(id)
                }
            } else {
                if (visibilityCheck.checkVisibility(consumer, value)) {
                    create(id, value)
                }
            }
        }

        override fun objectRemoved(id: String) {
            if (hasObject.contains(id)) {
                remove(id)
            }
        }

        private fun create(id: String, value: ToType) {
            hasObject.add(id)
            delegate.objectCreated(id, value)
        }

        private fun update(id: String, value: ToType) {
            delegate.objectUpdated(id, value)
        }

        private fun remove(id: String) {
            hasObject.remove(id)
            delegate.objectRemoved(id)
        }
    }
}
