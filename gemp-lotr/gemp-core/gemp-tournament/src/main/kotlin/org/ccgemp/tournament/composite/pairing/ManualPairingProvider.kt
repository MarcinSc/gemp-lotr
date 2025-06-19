package org.ccgemp.tournament.composite.pairing

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.TournamentClientInfo
import org.ccgemp.tournament.TournamentInterface
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.composite.CompositeTournamentUnloadNotified

/**
 * Example configuration
 * {@code
 * {
 *      type: manual
 * }
 * }
 */
@Exposes(LifecycleObserver::class, CompositeTournamentUnloadNotified::class, ManualPairing::class)
class ManualPairingProvider : LifecycleObserver, CompositeTournamentUnloadNotified, ManualPairing {
    @Inject
    private lateinit var registry: TournamentPairingRegistry

    @Inject
    private lateinit var tournamentInterface: TournamentInterface

    private val tournamentPairings = mutableMapOf<String, MutableMap<Int, RoundPairing>>()

    override fun afterContextStartup() {
        val pairingProvider: (JsonWithConfig<PairingConfig>) -> Pairing = {
            tournamentPairings[it.config.tournamentId] = mutableMapOf()
            ManualPairing(it.config.tournamentId)
        }

        registry.register("manual", pairingProvider)
    }

    override fun tournamentUnloaded(tournamentId: String) {
        tournamentPairings.remove(tournamentId)
    }

    override fun pairRound(tournamentId: String, round: Int, pairing: RoundPairing): Boolean {
        val pairings = tournamentPairings[tournamentId] ?: return false
        val tournament = tournamentInterface.findTournament(tournamentId) ?: return false
        if (!isValidPairings(tournament, pairing)) {
            return false
        }
        pairings[round] = pairing
        return true
    }

    private fun isValidPairings(tournament: TournamentClientInfo, roundPairing: RoundPairing): Boolean {
        val notDroppedPlayers = tournament.players.filter { !it.dropped }.map { it.player }

        val allPairedPlayers = roundPairing.pairings.flatMapTo(mutableSetOf()) { listOf(it.first, it.second) }
        // Player is not duplicated in pairings
        if (allPairedPlayers.size != roundPairing.pairings.size * 2) {
            return false
        }
        roundPairing.pairings.forEach { pairing ->
            // Paired players are not dropped
            if (pairing.first !in notDroppedPlayers || pairing.second !in notDroppedPlayers) {
                return false
            }
        }
        roundPairing.byes.forEach { bye ->
            // Players with byes are not dropped
            if (bye !in notDroppedPlayers) {
                return false
            }
            // Players with byes are not paired in this round
            if (bye in allPairedPlayers) {
                return false
            }
        }
        // Every not dropped player is either paired or has a bye
        if (notDroppedPlayers.any { it !in allPairedPlayers || it !in roundPairing.byes }) {
            return false
        }

        return true
    }

    inner class ManualPairing(
        private val tournamentId: String,
    ) : Pairing {
        override fun isReady(round: Int): Boolean {
            return tournamentPairings[tournamentId]?.get(round) != null
        }

        override fun createPairings(
            round: Int,
            players: List<TournamentParticipant>,
            matches: List<TournamentMatch>,
            pairingGroups: Map<Int, String>,
            byeGroups: Map<Int, String>,
        ): RoundPairing? {
            return tournamentPairings[tournamentId]?.get(round)
        }

        override fun shouldDropLoser(
            round: Int,
            player: String,
            players: List<TournamentParticipant>,
            matches: List<TournamentMatch>,
        ): Boolean {
            return false
        }
    }
}
