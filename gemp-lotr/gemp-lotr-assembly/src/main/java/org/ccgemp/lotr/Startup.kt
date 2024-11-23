package org.ccgemp.lotr

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.decorator.SimpleThreadPoolFactory
import com.gempukku.context.decorator.WorkerThreadExecutorSystem
import com.gempukku.context.initializer.inject.AnnotationSystemInitializer
import com.gempukku.context.initializer.inject.property.YamlPropertyResolver
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import com.gempukku.context.update.UpdatingSystem
import com.gempukku.server.chat.createChatSystems
import com.gempukku.server.login.CookieLoggedUserSystem
import com.gempukku.server.netty.NettyServerSystem
import com.gempukku.server.polling.LongPollingSystem
import org.ccgemp.collection.createCollectionSystems
import org.ccgemp.db.DbAccessSystem
import org.ccgemp.deck.createDeckSystems
import org.ccgemp.game.createGameSystems
import org.ccgemp.json.createJsonSystems
import org.ccgemp.server.player.createPlayerSystems
import org.ccgemp.tournament.createTournamentSystems
import org.ccgemp.transfer.createTransferSystems
import java.util.concurrent.Executors

fun main() {
    val rootPropertyResolver = YamlPropertyResolver("classpath:/root-context-config.yaml")
    val threadPoolFactory = SimpleThreadPoolFactory("Worker-Thread")
    val executorService = Executors.newSingleThreadScheduledExecutor(threadPoolFactory)

    val rootWorkerThreadExecutorSystem = WorkerThreadExecutorSystem(threadPoolFactory, executorService)
    val rootLifecycleSystem = LifecycleSystem()

    val rootSystems =
        listOf(
            // Provides access to scheduling tasks
            rootWorkerThreadExecutorSystem,
            // Responsible for managing state of the Context
            rootLifecycleSystem,
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

    val rootContext =
        DefaultGempukkuContext(
            null,
            AnnotationSystemResolver(
                rootSystems +
                        createPlayerSystems(),
            ),
            AnnotationSystemInitializer(rootPropertyResolver),
            rootWorkerThreadExecutorSystem,
        )

    rootContext.initialize()

    rootLifecycleSystem.start()


    val lotrPropertyResolver = YamlPropertyResolver("classpath:/lotr-context-config.yaml")

    val lotrWorkerThreadExecutorSystem = WorkerThreadExecutorSystem(threadPoolFactory, executorService)
    val lotrLifecycleSystem = LifecycleSystem()

    val lotrBaseSystems =
        listOf(
            // Provides access to scheduling tasks
            lotrWorkerThreadExecutorSystem,
            // Responsible for managing state of the Context
            lotrLifecycleSystem,
            // Responsible for processing ticks within the Context
            UpdatingSystem(),
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
            // Tournament related
            LotrTournamentRenderer(),
            // Collection related
            LotrProductLibrary(),
            LotrCollectionContentsSerializer(),
        )

    val lotrContext =
        DefaultGempukkuContext(
            rootContext,
            AnnotationSystemResolver(
                lotrBaseSystems +
                        createJsonSystems() +
                        createChatSystems() +
                        createTransferSystems() +
                        createCollectionSystems() +
                        createDeckSystems() +
                        createTournamentSystems() +
                        createGameSystems() +
                        lotrSpecificSystems,
            ),
            AnnotationSystemInitializer(lotrPropertyResolver),
            lotrWorkerThreadExecutorSystem,
        )

    lotrContext.initialize()

    lotrLifecycleSystem.start()
}
