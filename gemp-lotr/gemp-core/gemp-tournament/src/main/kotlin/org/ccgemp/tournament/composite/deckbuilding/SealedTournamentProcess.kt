package org.ccgemp.tournament.composite.deckbuilding

import org.ccgemp.collection.CollectionChange
import org.ccgemp.collection.CollectionInterface
import org.ccgemp.collection.ProductBox
import org.ccgemp.common.DeckValidator
import org.ccgemp.common.GameDeck
import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentProgress
import org.ccgemp.tournament.composite.RegisterDeckTournamentProcess
import org.ccgemp.tournament.composite.kickoff.Kickoff

const val DECK_BUILDING = "DECK_BUILDING"
const val AWAITING_PRODUCT = "AWAITING_PRODUCT"

class SealedTournamentProcess(
    private val collectionInterface: CollectionInterface,
    private val collectionType: String,
    private val product: ProductBox,
    private val deckType: String,
    private val deckValidator: DeckValidator,
    private val productKickoff: Kickoff,
    private val buildEndKickoff: Kickoff,
) : RegisterDeckTournamentProcess {
    override val deckTypes: List<String>
        get() = listOf(deckType)

    override fun canRegisterDecks(
        round: Int,
        stage: String,
        players: List<TournamentParticipant>,
        player: String,
        decks: List<GameDeck>,
    ): Boolean {
        if (stage == DECK_BUILDING && decks.size == 1) {
            val deck = decks[0]
            if (!deckValidator.isValid(deck)) {
                return false
            }
            val sealedCollection = collectionInterface.findPlayerCollection(player, collectionType) ?: return false
            val cardCounts = deck.deckParts.flatMap { it.value }
            cardCounts.forEach {
                if (sealedCollection.getItemCount(it.card) < it.count) {
                    return false
                }
            }
            return true
        }
        return false
    }

    override fun processTournament(
        round: Int,
        stage: String,
        players: List<TournamentParticipant>,
        matches: List<TournamentMatch>,
        pairingGroups: Map<Int, String>,
        byeGroups: Map<Int, String>,
        tournamentProgress: TournamentProgress,
    ) {
        when (stage) {
            "" -> {
                tournamentProgress.updateState(round, AWAITING_PRODUCT)
            }

            AWAITING_PRODUCT -> {
                if (productKickoff.isKickedOff(round)) {
                    giveProductToNotDroppedPlayers(players)
                    tournamentProgress.updateState(round, DECK_BUILDING)
                }
            }

            DECK_BUILDING -> {
                if (buildEndKickoff.isKickedOff(round)) {
                    dropPlayersWithoutDeck(players, tournamentProgress)
                    tournamentProgress.updateState(round, FINISHED_STAGE)
                }
            }
        }
    }

    private fun giveProductToNotDroppedPlayers(players: List<TournamentParticipant>) {
        players.filter { !it.dropped }.forEach {
            val sealedCollection = product.openPack()
            collectionInterface.addToPlayerCollection(it.player, collectionType, CollectionChange(true, "Sealed tournament", sealedCollection))
        }
    }

    private fun dropPlayersWithoutDeck(players: List<TournamentParticipant>, tournamentProgress: TournamentProgress) {
        players.filter { !it.dropped }.filter { !it.decks.containsKey(deckType) }.forEach {
            tournamentProgress.dropPlayer(it.player)
        }
    }

    override fun getTournamentStatus(stage: String): String {
        return when (stage) {
            "", AWAITING_PRODUCT -> "Awaiting product distribution"
            DECK_BUILDING -> "Deck building"
            else -> "Unknown"
        }
    }
}
