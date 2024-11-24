package org.ccgemp.state

import com.gempukku.context.Registration
import com.gempukku.ostream.ObjectStream
import com.gempukku.ostream.ObjectStreamCustomizer
import kotlin.reflect.KClass

interface ServerStateInterface {
    fun <ObjectType : Any> registerProducer(type: String, clazz: KClass<ObjectType>): ObjectStream<ObjectType>
    fun <FromType, ToType> registerCustomizer(type: String, customizer: ObjectStreamCustomizer<String, FromType, ToType>)
    fun <ToType> registerConsumer(type: String, player: String, stream: ObjectStream<ToType>): Registration
}