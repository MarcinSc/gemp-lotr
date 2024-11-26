package org.ccgemp.state

import com.gempukku.context.Registration
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.ostream.DefaultObjectStreamRegistry
import com.gempukku.ostream.ObjectStream
import com.gempukku.ostream.ObjectStreamCustomizer
import kotlin.reflect.KClass

@Exposes(ServerStateInterface::class)
class ServerStateSystem : ServerStateInterface {
    private val registry = DefaultObjectStreamRegistry<String>()

    override fun <ToType> registerConsumer(type: String, player: String, stream: ObjectStream<ToType>): Registration {
        return registry.registerConsumer(type, player, stream as ObjectStream<Any>)
    }

    override fun <ObjectType : Any> registerProducer(type: String, clazz: KClass<ObjectType>): ObjectStream<ObjectType> {
        return registry.registerProducer(type, clazz)
    }

    override fun <FromType, ToType> registerCustomizer(type: String, customizer: ObjectStreamCustomizer<String, FromType, ToType>) {
        registry.registerCustomizer(type, customizer as ObjectStreamCustomizer<String, Any, Any>)
    }
}
