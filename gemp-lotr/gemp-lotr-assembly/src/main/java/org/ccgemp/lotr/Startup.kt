package org.ccgemp.lotr

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.GempukkuContext
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
import org.ccgemp.collection.ExtendedDbCollectionRepository
import org.ccgemp.collection.createCollectionSystems
import org.ccgemp.db.DbAccessSystem
import org.ccgemp.deck.createDeckSystems
import org.ccgemp.format.createFormatSystems
import org.ccgemp.game.createGameSystems
import org.ccgemp.json.createJsonSystems
import org.ccgemp.lotr.chat.LegacyChatEventSinkProducer
import org.ccgemp.lotr.chat.LegacyChatNameDisplayFormatter
import org.ccgemp.lotr.collection.LegacyXmlCollectionModelRenderer
import org.ccgemp.lotr.collection.LotrProductLibrary
import org.ccgemp.lotr.deck.LegacyDeckModelRenderer
import org.ccgemp.lotr.deck.LegacyDeckShareApiSystem
import org.ccgemp.lotr.deck.LegacyHtmlDeckApiSystem
import org.ccgemp.lotr.deck.LegacyLibrarianDeckApiSystem
import org.ccgemp.lotr.deck.LotrDbDeckSerialization
import org.ccgemp.lotr.deck.LotrDeckDeserializer
import org.ccgemp.lotr.deck.LotrHtmlDeckSerializer
import org.ccgemp.lotr.format.LotrFormats
import org.ccgemp.lotr.game.LegacyGameProducer
import org.ccgemp.lotr.game.LotrGameEventSinkProducer
import org.ccgemp.lotr.game.LotrGameObserveSettingsExtractor
import org.ccgemp.lotr.tournament.LegacyTournamentModelRenderer
import org.ccgemp.lotr.transfer.LegacyXmlTransferModelRenderer
import org.ccgemp.server.player.createPlayerSystems
import org.ccgemp.tournament.createTournamentSystems
import org.ccgemp.transfer.createTransferSystems
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

fun main() {
    val threadPoolFactory = SimpleThreadPoolFactory("Worker-Thread")
    val executorService = Executors.newSingleThreadScheduledExecutor(threadPoolFactory)

    val (rootLifecycleSystem, rootContext) = createRootContext(threadPoolFactory, executorService)

    rootLifecycleSystem.start()

    val (lotrLifecycleSystem, lotrContext) = createLotrContext(threadPoolFactory, executorService, rootContext)

    lotrLifecycleSystem.start()
}

private fun createRootContext(
    threadPoolFactory: SimpleThreadPoolFactory,
    executorService: ScheduledExecutorService,
    parentContext: GempukkuContext? = null,
): Pair<LifecycleSystem, DefaultGempukkuContext> {
    val rootPropertyResolver = YamlPropertyResolver("classpath:/root-context-config.yaml")
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
            parentContext,
            AnnotationSystemResolver(
                rootSystems,
            ),
            AnnotationSystemInitializer(rootPropertyResolver),
            rootWorkerThreadExecutorSystem,
        )

    rootContext.initialize()
    return Pair(rootLifecycleSystem, rootContext)
}

private fun createLotrContext(
    threadPoolFactory: SimpleThreadPoolFactory,
    executorService: ScheduledExecutorService,
    parentContext: GempukkuContext,
): Pair<LifecycleSystem, GempukkuContext> {
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
            // Legacy deck APIs
            LegacyDeckShareApiSystem(),
            LegacyHtmlDeckApiSystem(),
            LegacyLibrarianDeckApiSystem(),
            LotrHtmlDeckSerializer(),
        )

    val lotrContext =
        DefaultGempukkuContext(
            parentContext,
            AnnotationSystemResolver(
                lotrBaseSystems +
                    createFormatSystems(LotrFormats()) +
                    createPlayerSystems() +
                    createJsonSystems() +
                    createChatSystems(LegacyChatEventSinkProducer()) +
                    createTransferSystems(LegacyXmlTransferModelRenderer()) +
                    createCollectionSystems(ExtendedDbCollectionRepository(), LegacyXmlCollectionModelRenderer(), LotrProductLibrary()) +
                    createDeckSystems(LegacyDeckModelRenderer(), LotrDeckDeserializer(), LotrDbDeckSerialization()) +
                    createTournamentSystems(LegacyTournamentModelRenderer()) +
                    createGameSystems(LotrGameEventSinkProducer(), LotrGameObserveSettingsExtractor(), LegacyGameProducer()) +
                    lotrSpecificSystems,
            ),
            AnnotationSystemInitializer(lotrPropertyResolver),
            lotrWorkerThreadExecutorSystem,
        )

    lotrContext.initialize()
    return Pair(lotrLifecycleSystem, lotrContext)
}
