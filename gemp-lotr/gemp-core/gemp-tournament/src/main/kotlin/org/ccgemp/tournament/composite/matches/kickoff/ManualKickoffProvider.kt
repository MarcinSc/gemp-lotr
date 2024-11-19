package org.ccgemp.tournament.composite.matches.kickoff

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.composite.CompositeTournamentUnloadNotified

@Exposes(LifecycleObserver::class, CompositeTournamentUnloadNotified::class)
class ManualKickoffProvider : LifecycleObserver, CompositeTournamentUnloadNotified {
    @Inject
    private lateinit var registry: TournamentKickoffRegistry

    private val tournamentKickoffs = mutableMapOf<String, MutableSet<Int>>()

    override fun afterContextStartup() {
        val kickoffProvider: (JsonWithConfig<KickoffConfig>) -> Kickoff = {
            tournamentKickoffs[it.config.tournamentId] = mutableSetOf()
            ManualKickoff(it.config.tournamentId)
        }

        registry.register("manual", kickoffProvider)
    }

    override fun tournamentUnloaded(tournamentId: String) {
        tournamentKickoffs.remove(tournamentId)
    }

    private fun kickoffRound(tournamentId: String, round: Int) {
        tournamentKickoffs[tournamentId]?.add(round)
    }

    inner class ManualKickoff(
        private val tournamentId: String,
    ) : Kickoff {
        override fun isKickedOff(round: Int): Boolean {
            return tournamentKickoffs[tournamentId]?.contains(round) ?: false
        }
    }
}
