package org.ccgemp.tournament.composite.misc

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.deck.DeckInterface
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.composite.TournamentProcess
import org.ccgemp.tournament.composite.TournamentProcessConfig
import org.ccgemp.tournament.composite.TournamentProcessRegistry
import org.ccgemp.tournament.composite.matches.standing.StandingsConfig
import org.ccgemp.tournament.composite.matches.standing.TournamentStandingsRegistry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Exposes(LifecycleObserver::class)
class MiscTournamentProcesses : LifecycleObserver {
    @Inject
    private lateinit var processRegistry: TournamentProcessRegistry

    @Inject
    private lateinit var standingsRegistry: TournamentStandingsRegistry

    @Inject
    private lateinit var deckInterface: DeckInterface

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
                LocalDateTime.parse(
                    def.getString("until", null),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                ),
            )
        }
        processRegistry.register("signup", signup)
    }
}
