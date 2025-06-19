package org.ccgemp.lotr.league

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.league.FixedLeaguePrizes
import org.ccgemp.collection.CollectionInterface
import org.ccgemp.common.TimeProvider
import org.ccgemp.league.LeagueHandler
import org.ccgemp.league.LeagueInterface
import org.ccgemp.lotr.LegacyObjectsProvider

@Exposes(LifecycleObserver::class)
class LegacyLeagueHandlers: LifecycleObserver {
    @Inject
    private lateinit var timeProvider: TimeProvider

    @Inject
    private lateinit var leagueInterface: LeagueInterface

    @Inject
    private lateinit var collectionInterface: CollectionInterface

    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    override fun afterContextStartup() {
        leagueInterface.registerLeagueHandler("constructed",
            ConstructedLeagueHandler(
                timeProvider,
                collectionInterface,
                legacyObjectsProvider.formatLibrary,
                FixedLeaguePrizes(legacyObjectsProvider.productLibrary),
                "permanent"
            ) as LeagueHandler<Any>)
    }
}