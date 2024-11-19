package org.ccgemp.tournament.composite.matches.pairing

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.composite.CompositeTournamentUnloadNotified

@Exposes(LifecycleObserver::class, CompositeTournamentUnloadNotified::class)
class ManualPairingProvider : LifecycleObserver, CompositeTournamentUnloadNotified {
    @Inject
    private lateinit var registry: TournamentPairingRegistry

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

    inner class ManualPairing(
        private val tournamentId: String,
    ) : Pairing {
        override fun createPairings(
            round: Int,
            players: List<TournamentParticipant>,
            matches: List<TournamentMatch>,
            pairingGroups: Map<Int, String>,
            byeGroups: Map<Int, String>,
        ): RoundPairing? {
            return tournamentPairings[tournamentId]?.get(round)
        }

        override fun isReady(round: Int): Boolean {
            return tournamentPairings[tournamentId]?.get(round) != null
        }
    }
}
