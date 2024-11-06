package org.ccgemp.tournament

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.server.HttpProcessingException
import org.ccgemp.deck.DeckInterface
import org.ccgemp.game.GameContainerInterface
import org.ccgemp.deck.GameDeck
import org.ccgemp.deck.toDecksString
import org.ccgemp.deck.toMultipleDecks
import org.ccgemp.game.GameParticipant
import java.time.LocalDateTime

const val FINISHED_STAGE = "FINISHED"

@Exposes(UpdatedSystem::class, LifecycleObserver::class, TournamentInterface::class)
class TournamentSystem : TournamentInterface, UpdatedSystem, LifecycleObserver {
    @Inject
    private lateinit var repository: TournamentRepository

    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var gameContainerInterface: GameContainerInterface<*>

    @InjectValue(value = "tournament.linger.hours")
    private var tournamentLingerHours: Long = 12

    private val handlerMap = mutableMapOf<String, TournamentHandler<Any>>()

    private val loadedTournaments = mutableMapOf<String, DefaultTournamentInfo<Any>>()

    override fun afterContextStartup() {
        repository.getUnfinishedOrStartAfter(LocalDateTime.now().minusHours(tournamentLingerHours)).forEach { tournament ->
            val tournamentHandler = findHandler(tournament.type)

            val data = tournamentHandler.initializeTournament(tournament)

            val tournamentInfo =
                DefaultTournamentInfo(
                    tournamentHandler,
                    tournament.startDate,
                    data,
                    tournament.tournamentId,
                    tournament.name,
                    tournament.stage,
                    tournament.round,
                    mutableListOf(),
                    mutableListOf(),
                    mutableSetOf(),
                )

            val tournamentPlayers = repository.getTournamentPlayers(tournament.tournamentId)
            tournamentPlayers.forEach {
                tournamentInfo.players.add(it.toParticipant())
            }
            val tournamentMatches = repository.getTournamentMatches(tournament.tournamentId)
            tournamentMatches.forEach {
                tournamentInfo.matches.add(it)
            }

            loadedTournaments[tournament.tournamentId] = tournamentInfo

            // Start all the matches that are not finished
            tournamentMatches.filter { !it.finished && !it.bye }.forEach { match ->
                startMatch(match.round, match.playerOne, match.playerTwo, tournamentInfo)
            }
        }
    }

    private fun findHandler(type: String): TournamentHandler<Any> {
        val tournamentHandler =
            handlerMap[type.lowercase()] ?: throw IllegalStateException("Handler for tournament type $type not found")
        return tournamentHandler
    }

    override fun registerTournamentHandler(type: String, tournamentHandler: TournamentHandler<Any>) {
        handlerMap[type.lowercase()] = tournamentHandler
    }

    override fun getLiveTournaments(): List<TournamentClientInfo> {
        return loadedTournaments.values.filter { it.status != FINISHED_STAGE }.toList()
    }

    override fun getHistoricTournaments(): List<TournamentClientInfo> {
        return loadedTournaments.values.filter { it.status == FINISHED_STAGE }.toList()
    }

    override fun getTournament(tournamentId: String): TournamentClientInfo? {
        return loadedTournaments[tournamentId]
    }

    override fun joinTournament(tournamentId: String, player: String, deckNames: List<String>, forced: Boolean) {
        val tournament = loadedTournaments[tournamentId]
        if (tournament == null || tournament.finished)
            throw HttpProcessingException(404)

        val decks = deckNames.map {
            deckInterface.findDeck(player, it)
        }.toMutableList()

        if (tournament.handler.canJoinTournament(tournament, player, decks, forced)) {
            repository.addPlayer(tournamentId, player, decks.toDecksString())
            tournament.players.add(TournamentParticipant(player, decks))
        } else {
            throw HttpProcessingException(403)
        }
    }

    override fun leaveTournament(tournamentId: String, player: String) {
        val tournament = loadedTournaments[tournamentId]
        if (tournament == null || tournament.finished)
            throw HttpProcessingException(404)

        repository.dropPlayer(tournamentId, player)
        tournament.players.firstOrNull { it.player == player }?.dropped = true
    }

    override fun registerDeck(tournamentId: String, player: String, deckName: String, forced: Boolean) {
        val tournament = loadedTournaments[tournamentId]
        if (tournament == null || tournament.finished)
            throw HttpProcessingException(404)

        val deck = deckInterface.findDeck(player, deckName) ?: throw HttpProcessingException(404)

        val participant = tournament.players.firstOrNull { it.player == player }
        if (participant == null)
            throw HttpProcessingException(404)

        if (tournament.handler.canRegisterDeck(tournament, player, deck, forced)) {
            val deckIndex = tournament.handler.getPlayerDeckIndex(tournament, player, tournament.round)
            participant.decks.expandToSet(deckIndex, deck)
            repository.updateDecks(tournamentId, player, participant.decks.toDecksString())
        } else {
            throw HttpProcessingException(403)
        }
    }

    override fun update() {
        val tournamentsToUnload = mutableSetOf<String>()
        loadedTournaments.forEach { (tournamentId, tournamentInfo) ->
            val handler = tournamentInfo.handler
            if (tournamentInfo.stage != FINISHED_STAGE) {
                handler.progressTournament(tournamentInfo, DefaultTournamentProgress(tournamentInfo))
            }
            if (tournamentInfo.stage == FINISHED_STAGE && tournamentInfo.startDate.isBefore(LocalDateTime.now().minusHours(tournamentLingerHours))) {
                handler.unloadTournament(tournamentInfo)
                tournamentsToUnload.add(tournamentId)
            }
        }
        tournamentsToUnload.forEach {
            loadedTournaments.remove(it)
        }
    }

    private fun startMatch(
        round: Int,
        playerOne: String,
        playerTwo: String,
        tournament: DefaultTournamentInfo<Any>,
    ) {
        val handler = tournament.handler

        val deckOne = tournament.players.firstOrNull { it.player == playerOne }!!.decks[handler.getPlayerDeckIndex(tournament, playerOne, round)]!!
        val deckTwo = tournament.players.firstOrNull { it.player == playerTwo }!!.decks[handler.getPlayerDeckIndex(tournament, playerTwo, round)]!!
        val participants =
            arrayOf(
                GameParticipant(playerOne, deckOne),
                GameParticipant(playerTwo, deckTwo),
            )
        val gameId = gameContainerInterface.createNewGame(participants, handler.getGameSettings(tournament, round))
        tournament.runningGames.add(gameId)
    }

    private fun TournamentPlayer.toParticipant(): TournamentParticipant {
        return TournamentParticipant(
            player,
            decks.toMultipleDecks().toMutableList(),
            dropped,
        )
    }

    private fun <T> MutableList<T?>.expandToSet(index: Int, value: T) {
        if (index < size) {
            set(index, value)
        } else {
            val toCreate = size - index + 1
            (1..toCreate).forEach { _ -> add(null) }
            set(index, value)
        }
    }

    inner class DefaultTournamentProgress(
        private val info: DefaultTournamentInfo<Any>,
    ) : TournamentProgress {
        override fun updateState(round: Int, stage: String) {
            repository.setStage(info.id, stage)
            repository.setRound(info.id, round)
            info.stage = stage
            info.round = round
        }

        override fun createMatch(recipe: TournamentGameRecipe) {
            val playerOne = recipe.participants[0]
            val playerTwo = recipe.participants[1]
            val round = recipe.round

            repository.createMatch(info.id, round, playerOne, playerTwo)

            startMatch(round, playerOne, playerTwo, info)

            info.matches.add(TournamentMatch(info.id, recipe.round, playerOne, playerTwo, null))
        }

        override fun awardBye(round: Int, player: String) {
            repository.createMatch(info.id, round, player, BYE_NAME, player)

            info.matches.add(TournamentMatch(info.id, round, player, BYE_NAME, player))
        }

        override fun dropPlayer(player: String) {
            repository.dropPlayer(info.id, player)
            info.players.firstOrNull { it.player == player }?.dropped = true
        }
    }

    data class DefaultTournamentInfo<TournamentData>(
        val handler: TournamentHandler<TournamentData>,
        override val startDate: LocalDateTime,
        override val data: TournamentData,
        override val id: String,
        override val name: String,
        override var stage: String,
        override var round: Int,
        override val players: MutableList<TournamentParticipant>,
        override val matches: MutableList<TournamentMatch>,
        val runningGames: MutableSet<String>,
    ) : TournamentInfo<TournamentData>, TournamentClientInfo {
        override val status: String
            get() = handler.getTournamentStatus(this)
        override val finished: Boolean
            get() = stage == FINISHED_STAGE
    }
}
