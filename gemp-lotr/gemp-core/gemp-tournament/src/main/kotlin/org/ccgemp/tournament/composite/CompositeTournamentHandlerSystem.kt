package org.ccgemp.tournament.composite

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.game.GameDeck
import org.ccgemp.game.GameSettings
import org.ccgemp.json.JsonProvider
import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.Tournament
import org.ccgemp.tournament.TournamentHandler
import org.ccgemp.tournament.TournamentInfo
import org.ccgemp.tournament.TournamentInterface
import org.ccgemp.tournament.TournamentProgress
import org.hjson.JsonObject

@Exposes(TournamentProcessRegistry::class)
class CompositeTournamentHandlerSystem : TournamentHandler<TournamentPlan>, TournamentProcessRegistry, LifecycleObserver {
    @Inject
    private lateinit var tournamentInterface: TournamentInterface

    @Inject
    private lateinit var jsonProvider: JsonProvider

    @InjectValue("tournament.composite.type")
    private var tournamentCompositeType: String = "composite"

    private val processProviders: MutableMap<String, Pair<JsonObject, Int>.() -> TournamentProcess> = mutableMapOf()

    override fun registerProcess(type: String, provider: Pair<JsonObject, Int>.() -> TournamentProcess) {
        processProviders[type.lowercase()] = provider
    }

    override fun afterContextStartup() {
        tournamentInterface.registerTournamentHandler(tournamentCompositeType, this as TournamentHandler<Any>)
    }

    override fun initializeTournament(tournament: Tournament): TournamentPlan {
        val tournamentPlan = TournamentPlan()
        jsonProvider.readJson(tournament.parameters).get("processes").asArray().map { it.asObject() }.forEach {
            val type = it.getString("type", null)
            tournamentPlan.addProcess(processProviders[type.lowercase()]!!.invoke(it to 1 + tournamentPlan.rounds))
        }
        return tournamentPlan
    }

    override fun getPlayerDeck(tournament: TournamentInfo<TournamentPlan>, player: String, round: Int): GameDeck {
        val plan = tournament.data
        val deckIndex = plan.getDeckIndex(round)
        return getDeck(
            tournament.players.first {
                it.player == player
            }.deck,
            deckIndex,
        )
    }

    override fun getGameSettings(tournament: TournamentInfo<TournamentPlan>, round: Int): GameSettings {
        val plan = tournament.data
        return plan.getGameSettings(round)
    }

    override fun progressTournament(tournament: TournamentInfo<TournamentPlan>, tournamentProgress: TournamentProgress) {
        tournament.data.progressTournament(tournament, tournamentProgress)
    }

    override fun getTournamentStatus(tournament: TournamentInfo<TournamentPlan>): String {
        if (tournament.stage == FINISHED_STAGE) {
            return "Finished"
        }
        return tournament.data.getTournamentStatus(tournament)
    }

    override fun unloadTournament(tournament: TournamentInfo<TournamentPlan>) {
    }
}
