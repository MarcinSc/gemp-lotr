package org.ccgemp.tournament.composite.misc

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.deck.DeckInterface
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.composite.TournamentProcess
import org.ccgemp.tournament.composite.TournamentProcessConfig
import org.ccgemp.tournament.composite.TournamentProcessRegistry
import org.ccgemp.tournament.composite.kickoff.KickoffConfig
import org.ccgemp.tournament.composite.kickoff.TournamentKickoffRegistry
import org.ccgemp.tournament.composite.standing.StandingsConfig
import org.ccgemp.tournament.composite.standing.TournamentStandingsRegistry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Example configuration
 * {@code
 * {
 *      type: cutToTopX
 *      x: 8
 *      standings: {
 *          type: modifiedMedian
 *      }
 * }
 * }
 * {@code
 * {
 *      type: cutToTopX
 *      x: 8
 *      standings: {
 *          type: modifiedMedian
 *      }
 * }
 * }
 * {@code
 * {
 *      type: pause
 *      # time in milliseconds
 *      time: 30000
 * }
 * }
 * {@code
 * {
 *      type: pauseUntil
 *      # Time in format "yyyy-MM-dd HH:mm:ss" in UTC time zone
 *      time: 2024-11-23 00:00:00
 *      x: 8
 *      standings: {
 *          type: modifiedMedian
 *      }
 * }
 * }
 * {@code
 * {
 *      type: signup
 *      # Optional array of invited players, if specified - only those player will be able to join
 *      invitedPlayers: [
 *          playerOne
 *          playerTwo
 *      ]
 *      # Array of formats that you have to register decks for during signup - used for deck validation
 *      formats: [
 *          fotr_block
 *      ]
 *      # Array of deck types, as used in tournament configuration to identify decks to play with
 *      deckTypes: [
 *          fotrBlockPortion
 *      ]
 * }
 * }
 */
@Exposes(LifecycleObserver::class)
class MiscTournamentProcesses : LifecycleObserver {
    @Inject
    private lateinit var processRegistry: TournamentProcessRegistry

    @Inject
    private lateinit var standingsRegistry: TournamentStandingsRegistry

    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var kickoffRegistry: TournamentKickoffRegistry

    override fun afterContextStartup() {
        val cutToTopX: (JsonWithConfig<TournamentProcessConfig>) -> TournamentProcess = {
            val def = it.json
            CutToTopX(
                def.getInt("x", 8),
                standingsRegistry.create(JsonWithConfig(def.get("standings").asObject(), StandingsConfig(it.config.tournamentId))),
            )
        }
        processRegistry.register("cutToTopX", cutToTopX)

        val pause: (JsonWithConfig<TournamentProcessConfig>) -> TournamentProcess = {
            val def = it.json
            Pause(
                def.getLong("time", 0),
            )
        }
        processRegistry.register("pause", pause)

        val pauseUntil: (JsonWithConfig<TournamentProcessConfig>) -> TournamentProcess = {
            val def = it.json
            PauseUntil(
                LocalDateTime.parse(
                    def.getString("time", null),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                ),
            )
        }
        processRegistry.register("pauseUntil", pauseUntil)

        val signup: (JsonWithConfig<TournamentProcessConfig>) -> TournamentProcess = {
            val def = it.json
            val allowedPlayers =
                def.get("invitedPlayers")?.asArray()?.mapTo(mutableSetOf()) {
                    it.asString()
                }
            val formats =
                def.get("formats").asArray().map {
                    it.asString()
                }
            val deckTypes =
                def.get("deckTypes").asArray().map {
                    it.asString()
                }
            Signup(
                allowedPlayers,
                deckTypes,
                formats.map { deckInterface.getValidator(it) },
                kickoffRegistry.create(JsonWithConfig(def.get("kickoff").asObject(), KickoffConfig(it.config.tournamentId))),
            )
        }
        processRegistry.register("signup", signup)
    }
}
