package org.ccgemp.tournament.composite.matches

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.game.GameSettings
import org.ccgemp.game.GameTimer
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.composite.TournamentProcess
import org.ccgemp.tournament.composite.TournamentProcessConfig
import org.ccgemp.tournament.composite.TournamentProcessRegistry
import org.ccgemp.tournament.composite.kickoff.KickoffConfig
import org.ccgemp.tournament.composite.kickoff.TournamentKickoffRegistry
import org.ccgemp.tournament.composite.pairing.PairingConfig
import org.ccgemp.tournament.composite.pairing.TournamentPairingRegistry

/**
 * Example configuration
 * {@code
 * {
 *      type: playGames
 *      # number of rounds
 *      rounds: 3
 *      # type of deck, as used in tournament definition
 *      deckType: constructedPortion
 *      # format used for playing games
 *      format: anythingGoesFotR
 *      # this defines which pairings cannot be duplicated across multiple rounds
 *      pairingGroup: wholeTournament
 *      # this defines which players cannot receive bye, as they already did in a round
 *      byeGroup: wholeTournament
 *      kickoff: {
 *          type: manual
 *      }
 *      pairing: {
 *          type: swiss
 *      }
 *      dropLosers: false
 * }
 * }
 */
@Exposes(LifecycleObserver::class)
class MatchTournamentProcesses : LifecycleObserver {
    @Inject
    private lateinit var processRegistry: TournamentProcessRegistry

    @Inject
    private lateinit var kickoffRegistry: TournamentKickoffRegistry

    @Inject
    private lateinit var pairingRegistry: TournamentPairingRegistry

    override fun afterContextStartup() {
        val constructedProcess: (JsonWithConfig<TournamentProcessConfig>) -> TournamentProcess = {
            val def = it.json
            PlayGamesTournamentProcess(
                it.config.startRound,
                def.getInt("rounds", 1),
                def.getString("deckType", null),
                GameSettings(
                    format = def.getString("format", null),
                    private = false,
                    watchable = false,
                    timeSettings = GameTimer("Competitive", 60 * 40, 60 * 5),
                    info = "Tournament match",
                    decisionLeniencySeconds = 3,
                ),
                def.getString("pairingGroup", "1"),
                def.getString("byeGroup", "1"),
                kickoffRegistry.create(JsonWithConfig(def.get("kickoff").asObject(), KickoffConfig(it.config.tournamentId))),
                pairingRegistry.create(JsonWithConfig(def.get("pairing").asObject(), PairingConfig(it.config.tournamentId))),
            )
        }
        processRegistry.register(
            "playGames",
            constructedProcess,
        )
    }
}
