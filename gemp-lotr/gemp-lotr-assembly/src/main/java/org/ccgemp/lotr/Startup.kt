package org.ccgemp.lotr

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.decorator.SimpleThreadPoolFactory
import com.gempukku.context.processor.inject.decorator.WorkerThreadExecutorSystem
import com.gempukku.context.processor.inject.property.YamlPropertyResolver
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import com.gempukku.context.update.UpdatingSystem
import com.gempukku.server.chat.ChatApiSystem
import com.gempukku.server.chat.ChatSystem
import com.gempukku.server.chat.polling.legacy.LegacyChatEventSinkProducer
import com.gempukku.server.login.CookieLoggedUserSystem
import com.gempukku.server.netty.NettyServerSystem
import com.gempukku.server.polling.LongPollingSystem
import org.ccgemp.db.DbAccessSystem
import org.ccgemp.server.player.DbPlayerRepository
import org.ccgemp.server.player.PlayerApiSystem
import org.ccgemp.server.player.PlayerSystem
import org.ccgemp.server.player.admin.AdminPlayerApiSystem
import org.ccgemp.server.player.admin.AdminPlayerSystem
import java.util.concurrent.Executors

fun main() {
    val lifecycleSystem = LifecycleSystem()
    val propertyResolver = YamlPropertyResolver("classpath:/server-config.yaml")
    val threadPoolFactory = SimpleThreadPoolFactory("Worker-Thread")
    val executorService = Executors.newSingleThreadScheduledExecutor(threadPoolFactory)

    val workerThreadExecutorSystem = WorkerThreadExecutorSystem(threadPoolFactory, executorService)

    val serverContext =
        DefaultGempukkuContext(
            null,
            AnnotationSystemResolver(),
            AnnotationSystemInjector(propertyResolver, workerThreadExecutorSystem),
            // Provides access to scheduling tasks
            workerThreadExecutorSystem,
            // Responsible for managing state of the Context
            lifecycleSystem,
            // Responsible for processing ticks within the Context
            UpdatingSystem(),
            // Responsible for managing player logged sessions
            CookieLoggedUserSystem(),
            // Responsible for providing access to APIs
            NettyServerSystem(),
            // Allows to communicate with clients using long-polling mechanism
            LongPollingSystem(),
            // Allows access to database
            DbAccessSystem(),
            // Responsible for chat server and its API
            ChatSystem(),
            ChatApiSystem(),
            LegacyChatEventSinkProducer(),
            LegacyChatNameDisplayFormatter(),
            // Responsible for player registration, login, etc.
            PlayerSystem(),
            PlayerApiSystem(),
            DbPlayerRepository(),
            // Responsible for administrating users
            AdminPlayerSystem(),
            AdminPlayerApiSystem(),
        ).initialize()

    lifecycleSystem.start()
}

private class ResourceLocator
