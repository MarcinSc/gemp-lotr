package org.ccgemp.lotr

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.decorator.WorkerThreadExecutorSystem
import com.gempukku.context.processor.inject.property.YamlPropertyResolver
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import com.gempukku.context.update.UpdatingSystem
import com.gempukku.server.chat.ChatApiSystem
import com.gempukku.server.chat.ChatSystem
import com.gempukku.server.login.LoggedUserSystem
import com.gempukku.server.netty.NettyServerSystem
import com.gempukku.server.polling.LongPollingSystem
import org.ccgemp.db.DbAccessSystem
import org.ccgemp.server.player.DbPlayerDAO
import org.ccgemp.server.player.PlayerApiSystem
import org.ccgemp.server.player.PlayerSystem

fun main() {
    val lifecycleSystem = LifecycleSystem()
    val propertyResolver =
        YamlPropertyResolver(ResourceLocator::class.java.getResourceAsStream("/server-config.yaml")!!)
    val systemDecorator = WorkerThreadExecutorSystem()

    val serverContext = DefaultGempukkuContext(
        null,
        AnnotationSystemResolver(), AnnotationSystemInjector(propertyResolver, systemDecorator),
        // Provides access to scheduling tasks
        systemDecorator,
        // Responsible for managing state of the Context
        lifecycleSystem,
        // Responsible for processing ticks within the Context
        UpdatingSystem(),
        // Responsible for managing player logged sessions
        LoggedUserSystem(),
        // Responsible for providing access to APIs
        NettyServerSystem(),
        // Allows to communicate with clients using long-polling mechanism
        LongPollingSystem(),
        // Allows access to database
        DbAccessSystem(),
        // Responsible for chat server and its API
        ChatSystem(), ChatApiSystem(),
        // Responsible for player registration, login, etc.
        PlayerSystem(), PlayerApiSystem(), DbPlayerDAO(),
    ).initialize()

    lifecycleSystem.start()
}

private class ResourceLocator