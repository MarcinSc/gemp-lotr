package org.ccgemp.tournament.composite.matches

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.game.GameSettings
import org.ccgemp.game.GameTimer
import org.ccgemp.tournament.composite.TournamentProcess
import org.ccgemp.tournament.composite.TournamentProcessRegistry
import org.ccgemp.tournament.composite.matches.kickoff.TournamentKickoffRegistry
import org.ccgemp.tournament.composite.matches.pairing.TournamentPairingRegistry
import org.hjson.JsonObject

@Exposes(LifecycleObserver::class)
class MatchTournamentProcesses : LifecycleObserver {
    @Inject
    private lateinit var processRegistry: TournamentProcessRegistry

    @Inject
    private lateinit var kickoffRegistry: TournamentKickoffRegistry

    @Inject
    private lateinit var pairingRegistry: TournamentPairingRegistry

    override fun afterContextStartup() {
        val constructedProcess: (Pair<JsonObject, Int>) -> TournamentProcess = {
            val def = it.first
            ConstructedTournamentProcess(
                it.second,
                def.getInt("rounds", 1),
                def.getInt("deckIndex", 0),
                GameSettings(
                    def.getString("format", null),
                    true,
                    GameTimer("Competitive", 60 * 40, 60 * 5),
                    "",
                    3,
                ),
                def.getString("pairingGroup", "1"),
                def.getString("byeGroup", "1"),
                kickoffRegistry.create(def.get("kickoff").asObject()),
                pairingRegistry.create(def.get("pairing").asObject()),
                def.getBoolean("dropLosers", false),
            )
        }
        processRegistry.registerProcess(
            "constructed",
            constructedProcess,
        )
    }
}
