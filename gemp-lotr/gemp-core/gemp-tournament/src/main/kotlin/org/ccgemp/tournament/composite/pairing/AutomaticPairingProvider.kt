package org.ccgemp.tournament.composite.pairing

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.composite.standing.StandingsConfig
import org.ccgemp.tournament.composite.standing.TournamentStandingsRegistry

@Exposes(LifecycleObserver::class)
class AutomaticPairingProvider: LifecycleObserver {
    @Inject
    private lateinit var registry: TournamentPairingRegistry

    @Inject
    private lateinit var standingsRegistry: TournamentStandingsRegistry

    override fun afterContextStartup() {
        val swissPairingProvider: (JsonWithConfig<PairingConfig>) -> Pairing = {
            SwissPairing(standingsRegistry.create(JsonWithConfig(it.json.get("standings").asObject(), StandingsConfig(it.config.tournamentId))))
        }
        registry.register("swiss", swissPairingProvider)

        val singleEliminationPairingProvider: (JsonWithConfig<PairingConfig>) -> Pairing = {
            SingleEliminationPairing(
                standingsRegistry.create(JsonWithConfig(it.json.get("standings").asObject(), StandingsConfig(it.config.tournamentId))),
                it.json.getInt("afterRound", 0),
            )
        }
        registry.register("singleElimination", singleEliminationPairingProvider)

        val doubleEliminationPairingProvider: (JsonWithConfig<PairingConfig>) -> Pairing = {
            SingleEliminationPairing(
                standingsRegistry.create(JsonWithConfig(it.json.get("standings").asObject(), StandingsConfig(it.config.tournamentId))),
                it.json.getInt("afterRound", 0),
            )
        }
        registry.register("doubleElimination", doubleEliminationPairingProvider)
    }
}