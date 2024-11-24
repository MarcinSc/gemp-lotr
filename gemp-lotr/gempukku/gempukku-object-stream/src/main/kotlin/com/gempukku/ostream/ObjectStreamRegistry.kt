package com.gempukku.ostream

import kotlin.reflect.KClass

interface ObjectStreamRegistry<ConsumerType> {
    fun <ObjectType : Any> registerProducer(type: String, clazz: KClass<ObjectType>): ObjectStream<ObjectType>

    fun <ObjectType: Any> registerConsumer(type: String, consumer: ConsumerType, stream: ObjectStream<ObjectType>): Runnable

    fun <FromType: Any, ToType: Any>registerCustomizer(type: String, customizer: ObjectStreamCustomizer<ConsumerType, FromType, ToType>)
}