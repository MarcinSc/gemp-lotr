package org.ccgemp.tournament.composite.deckbuilding

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.collection.CollectionInterface
import org.ccgemp.collection.ProductLibrary
import org.ccgemp.deck.DeckInterface
import org.ccgemp.deck.DeckValidator
import org.ccgemp.deck.GameDeck
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.composite.TournamentProcess
import org.ccgemp.tournament.composite.TournamentProcessConfig
import org.ccgemp.tournament.composite.TournamentProcessRegistry
import org.ccgemp.tournament.composite.kickoff.KickoffConfig
import org.ccgemp.tournament.composite.kickoff.TournamentKickoffRegistry

@Exposes(LifecycleObserver::class)
class DeckBuildingTournamentProcesses : LifecycleObserver {
    @Inject
    private lateinit var processRegistry: TournamentProcessRegistry

    @Inject
    private lateinit var collectionInterface: CollectionInterface

    @Inject
    private lateinit var productLibrary: ProductLibrary

    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var kickoffRegistry: TournamentKickoffRegistry

    override fun afterContextStartup() {
        val sealedTournamentProcess: (JsonWithConfig<TournamentProcessConfig>) -> TournamentProcess = {
            val def = it.json
            val collectionType = def.getString("collectionType", null)
            SealedTournamentProcess(
                collectionInterface,
                collectionType,
                productLibrary.getProductBox(def.getString("product", null))!!,
                def.getString("deckType", null),
                InCollectionValidator(deckInterface.getValidator(def.getString("format", null)), collectionType),
                kickoffRegistry.create(JsonWithConfig(def.get("productKickoff").asObject(), KickoffConfig(it.config.tournamentId))),
                kickoffRegistry.create(JsonWithConfig(def.get("buildEndKickoff").asObject(), KickoffConfig(it.config.tournamentId))),
            )
        }
        processRegistry.register(
            "sealed",
            sealedTournamentProcess,
        )
    }

    inner class InCollectionValidator(
        private val formatValidator: DeckValidator,
        private val collectionType: String,
    ) : DeckValidator {
        override fun isValid(player: String, deck: GameDeck): Boolean {
            return formatValidator.isValid(player, deck) && hasCardsInCollection(player, deck)
        }

        private fun hasCardsInCollection(player: String, deck: GameDeck): Boolean {
            val playerCollection = collectionInterface.getPlayerCollection(player, collectionType) ?: return false
            val cardCounts = deck.deckParts.flatMap { it.value }.groupingBy { it }.eachCount()
            cardCounts.forEach {
                if (playerCollection.getItemCount(it.key) < it.value) {
                    return false
                }
            }
            return true
        }
    }
}
