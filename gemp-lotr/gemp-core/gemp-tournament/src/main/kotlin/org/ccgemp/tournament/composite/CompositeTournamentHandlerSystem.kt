package org.ccgemp.tournament.composite

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectList
import com.gempukku.context.processor.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.deck.GameDeck
import org.ccgemp.game.GameSettings
import org.ccgemp.json.JsonProvider
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.json.TypedRegistry
import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.Tournament
import org.ccgemp.tournament.TournamentHandler
import org.ccgemp.tournament.TournamentInfo
import org.ccgemp.tournament.TournamentInterface
import org.ccgemp.tournament.TournamentProgress

@Exposes(TournamentProcessRegistry::class)
class CompositeTournamentHandlerSystem :
    TournamentHandler<TournamentPlan>,
    TournamentProcessRegistry,
    TypedRegistry<TournamentProcessConfig, TournamentProcess>(),
    LifecycleObserver {
    @Inject
    private lateinit var tournamentInterface: TournamentInterface

    @Inject
    private lateinit var jsonProvider: JsonProvider

    @InjectList
    private lateinit var tournamentUnloadNotified: List<CompositeTournamentUnloadNotified>

    @InjectValue("tournament.composite.type")
    private var tournamentCompositeType: String = "composite"

    override fun afterContextStartup() {
        tournamentInterface.registerTournamentHandler(tournamentCompositeType, this as TournamentHandler<Any>)
    }

    override fun initializeTournament(tournament: Tournament): TournamentPlan {
        val tournamentPlan = TournamentPlan()
        jsonProvider.readJson(tournament.parameters).get("processes").asArray().map { it.asObject() }.forEach {
            val process = create(JsonWithConfig(it, TournamentProcessConfig(tournament.tournamentId, 1 + tournamentPlan.rounds)))
            tournamentPlan.addProcess(process)
        }
        return tournamentPlan
    }

    override fun getPlayerDeckIndex(tournament: TournamentInfo<TournamentPlan>, player: String, round: Int): Int {
        val plan = tournament.data
        return plan.getDeckIndex(round)
    }

    override fun getGameSettings(tournament: TournamentInfo<TournamentPlan>, round: Int): GameSettings {
        val plan = tournament.data
        return plan.getGameSettings(round)
    }

    override fun canJoinTournament(
        tournament: TournamentInfo<TournamentPlan>,
        player: String,
        decks: List<GameDeck?>,
        forced: Boolean,
    ): Boolean {
        if (tournament.stage == FINISHED_STAGE) {
            return false
        }
        val plan = tournament.data
        return plan.canJoinTournament(tournament, player, decks, forced)
    }

    override fun canRegisterDeck(
        tournament: TournamentInfo<TournamentPlan>,
        player: String,
        deck: GameDeck,
        forced: Boolean,
    ): Boolean {
        if (tournament.stage == FINISHED_STAGE) {
            return false
        }
        val plan = tournament.data
        return plan.canRegisterDeck(tournament, player, deck, forced)
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
        tournamentUnloadNotified.forEach {
            it.tournamentUnloaded(tournament.id)
        }
    }
}
