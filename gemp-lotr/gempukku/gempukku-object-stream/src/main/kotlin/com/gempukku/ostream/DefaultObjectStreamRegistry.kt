package com.gempukku.ostream

import com.gempukku.context.Registration
import kotlin.reflect.KClass

class DefaultObjectStreamRegistry<ConsumerType> : ObjectStreamRegistry<ConsumerType> {
    private val customizersPerType: MutableMap<String, ObjectStreamCustomizer<ConsumerType, Any, Any>> = mutableMapOf()

    override fun <ObjectType : Any> registerConsumer(type: String, consumer: ConsumerType, stream: ObjectStream<ObjectType>): Registration {
        val customizer = customizersPerType[type] ?: throw IllegalStateException("Customizer not registered for $type")

        customizer.addConsumer(consumer, stream as ObjectStream<Any>)

        return object:Registration {
            override fun deregister() {
                customizer.removeConsumer(consumer)
            }
        }
    }

    override fun <ObjectType : Any> registerProducer(type: String, clazz: KClass<ObjectType>): ObjectStream<ObjectType> {
        return ProducerObjectStream(type)
    }

    override fun <FromType : Any, ToType : Any> registerCustomizer(type: String, customizer: ObjectStreamCustomizer<ConsumerType, FromType, ToType>) {
        if (customizersPerType.containsKey(type)) {
            throw IllegalArgumentException("Customizer $type is already registered")
        }
        customizersPerType[type] = customizer as ObjectStreamCustomizer<ConsumerType, Any, Any>
    }

    inner class ProducerObjectStream<ObjectType>(
        private val type: String,
    ) : ObjectStream<ObjectType> {
        override fun objectCreated(id: String, value: ObjectType) {
            customizersPerType[type]?.customizeCreated(id, value as Any)
        }

        override fun objectUpdated(id: String, value: ObjectType) {
            customizersPerType[type]?.customizeUpdated(id, value as Any)
        }

        override fun objectRemoved(id: String) {
            customizersPerType[type]?.customizeRemoved(id)
        }
    }
}
