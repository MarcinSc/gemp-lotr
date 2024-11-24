package com.gempukku.ostream

import com.gempukku.context.Registration
import kotlin.reflect.KClass

interface ObjectStreamRegistry<ConsumerType> {
    fun <ObjectType : Any> registerProducer(type: String, clazz: KClass<ObjectType>): ObjectStream<ObjectType>

    fun <ObjectType: Any> registerConsumer(type: String, consumer: ConsumerType, stream: ObjectStream<ObjectType>): Registration

    fun <FromType: Any, ToType: Any>registerCustomizer(type: String, customizer: ObjectStreamCustomizer<ConsumerType, FromType, ToType>)
}