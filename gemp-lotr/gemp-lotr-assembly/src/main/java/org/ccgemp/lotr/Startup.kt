package org.ccgemp.lotr

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.decorator.SimpleThreadPoolFactory
import com.gempukku.context.processor.inject.decorator.WorkerThreadExecutorSystem
import com.gempukku.context.processor.inject.property.YamlPropertyResolver
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import com.gempukku.context.update.UpdatingSystem
import com.gempukku.server.login.CookieLoggedUserSystem
import com.gempukku.server.netty.NettyServerSystem
import com.gempukku.server.polling.LongPollingSystem
import org.ccgemp.db.DbAccessSystem
import org.ccgemp.deck.createDeckSystems
import org.ccgemp.game.createGameSystems
import org.ccgemp.server.player.createPlayerSystems
import org.ccgemp.tournament.createTournamentSystems
import java.util.concurrent.Executors

fun main() {
    val lifecycleSystem = LifecycleSystem()
    val propertyResolver = YamlPropertyResolver("classpath:/server-config.yaml")
    val threadPoolFactory = SimpleThreadPoolFactory("Worker-Thread")
    val executorService = Executors.newSingleThreadScheduledExecutor(threadPoolFactory)

    val workerThreadExecutorSystem = WorkerThreadExecutorSystem(threadPoolFactory, executorService)

    val baseSystems =
        listOf(
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
        )

    val lotrSpecificSystems =
        listOf(
            // Provides access to card library, formats, etc
            DefaultLegacyObjectProvider(),
            // Legacy chat name display
            LegacyChatNameDisplayFormatter(),
            // Legacy game running
            LegacyGameProducer(),
            // Deck related
            LotrDeckSerialization(),
            LotrDeckValidation(),
        )

    val serverContext =
        DefaultGempukkuContext(
            null,
            AnnotationSystemResolver(),
            AnnotationSystemInjector(propertyResolver, workerThreadExecutorSystem),
            baseSystems +
                    createPlayerSystems() +
                    createDeckSystems() +
                    createTournamentSystems() +
                    createGameSystems() +
                    lotrSpecificSystems,
        )

    serverContext.initialize()

    lifecycleSystem.start()
}

private class ResourceLocator
