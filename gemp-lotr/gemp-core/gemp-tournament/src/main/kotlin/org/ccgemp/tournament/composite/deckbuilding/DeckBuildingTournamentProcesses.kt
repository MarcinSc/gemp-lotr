package org.ccgemp.tournament.composite.deckbuilding

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.collection.CollectionInterface
import org.ccgemp.collection.ProductLibrary
import org.ccgemp.format.GempFormats
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.composite.TournamentProcess
import org.ccgemp.tournament.composite.TournamentProcessConfig
import org.ccgemp.tournament.composite.TournamentProcessRegistry
import org.ccgemp.tournament.composite.kickoff.KickoffConfig
import org.ccgemp.tournament.composite.kickoff.TournamentKickoffRegistry

/**
 * Example configuration
 * {@code
 * {
 *      type: sealed
 *      # type of collection as stored in collection table
 *      collectionType: sealedTournament
 *      # product given on sealed portion start
 *      product: fellowshipOfTheRingSealed
 *      # type of deck, as used in tournament definition
 *      deckType: sealedDeck
 *      # format used for validating submitted deck
 *      format: anythingGoesFotR
 *      productKickoff: {
 *          type: manual
 *      }
 *      buildEndKickoff: {
 *          type: timed
 *          # 30 minutes
 *          pause: 1800000
 *      }
 * }
 * }
 */
@Exposes(LifecycleObserver::class)
class DeckBuildingTournamentProcesses : LifecycleObserver {
    @Inject
    private lateinit var processRegistry: TournamentProcessRegistry

    @Inject
    private lateinit var collectionInterface: CollectionInterface

    @Inject
    private lateinit var productLibrary: ProductLibrary

    @Inject
    private lateinit var gempFormats: GempFormats<Any>

    @Inject
    private lateinit var kickoffRegistry: TournamentKickoffRegistry

    override fun afterContextStartup() {
        val sealedTournamentProcess: (JsonWithConfig<TournamentProcessConfig>) -> TournamentProcess = {
            val def = it.json
            val collectionType = def.getString("collectionType", null)
            SealedTournamentProcess(
                collectionInterface,
                collectionType,
                productLibrary.findProductBox(def.getString("product", null))!!,
                def.getString("deckType", null),
                gempFormats.getValidator(def.getString("format", null)),
                kickoffRegistry.create(JsonWithConfig(def.get("productKickoff").asObject(), KickoffConfig(it.config.tournamentId))),
                kickoffRegistry.create(JsonWithConfig(def.get("buildEndKickoff").asObject(), KickoffConfig(it.config.tournamentId))),
            )
        }
        processRegistry.register(
            "sealed",
            sealedTournamentProcess,
        )
    }
}
