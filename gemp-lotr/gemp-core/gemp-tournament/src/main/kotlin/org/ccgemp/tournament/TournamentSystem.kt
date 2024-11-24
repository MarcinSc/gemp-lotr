package org.ccgemp.tournament

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.ostream.ObjectStream
import com.gempukku.server.HttpProcessingException
import org.ccgemp.deck.DeckInterface
import org.ccgemp.deck.GameDeck
import org.ccgemp.deck.toDeckParts
import org.ccgemp.deck.toDeckString
import org.ccgemp.game.GameContainerInterface
import org.ccgemp.game.GameParticipant
import org.ccgemp.game.GameResultListener
import org.ccgemp.game.GameSettings
import org.ccgemp.state.ServerStateInterface
import java.time.LocalDateTime

const val FINISHED_STAGE = "FINISHED"

@Exposes(UpdatedSystem::class, TournamentInterface::class, GameResultListener::class)
class TournamentSystem : TournamentInterface, UpdatedSystem, LifecycleObserver, GameResultListener {
    @Inject
    private lateinit var repository: TournamentRepository

    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var gameContainerInterface: GameContainerInterface<*>

    @Inject(allowsNull = true)
    private var serverState: ServerStateInterface? = null

    private var tournamentStream: ObjectStream<TournamentClientInfo>? = null

    @InjectValue(value = "tournament.linger.hours")
    private var tournamentLingerHours: Long = 12

    private var initialized: Boolean = false

    private val handlerMap = mutableMapOf<String, TournamentHandler<Any>>()

    private val loadedTournaments = mutableMapOf<String, DefaultTournamentInfo<Any>>()

    override fun afterContextStartup() {
        tournamentStream = serverState?.registerProducer("tournament", TournamentClientInfo::class)
    }

    private fun createTournamentState(tournament: TournamentClientInfo) {
        tournamentStream?.objectCreated(tournament.id, tournament)
    }

    private fun updateTournamentState(tournament: TournamentClientInfo) {
        tournamentStream?.objectUpdated(tournament.id, tournament)
    }

    private fun removeTournamentState(tournament: TournamentClientInfo) {
        tournamentStream?.objectRemoved(tournament.id)
    }

    private fun findHandler(type: String): TournamentHandler<Any> {
        val tournamentHandler =
            handlerMap[type.lowercase()] ?: throw IllegalStateException("Handler for tournament type $type not found")
        return tournamentHandler
    }

    override fun registerTournamentHandler(type: String, tournamentHandler: TournamentHandler<Any>) {
        handlerMap[type.lowercase()] = tournamentHandler
    }

    override fun addTournament(tournamentId: String, type: String, name: String, startDate: LocalDateTime, parameters: String): Boolean {
        val handler = handlerMap[type.lowercase()] ?: return false
        if (!handler.validateTournament(Tournament(tournamentId, name, startDate, type, parameters)))
            return false
        repository.createTournament(tournamentId, type, name, startDate, parameters, "", 0)
        addTournamentInternal(handler, Tournament(tournamentId, name, startDate, type, parameters))
        return true
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

    override fun joinTournament(tournamentId: String, player: String, deckNames: List<String>) {
        val tournament = loadedTournaments[tournamentId]
        if (tournament == null || tournament.finished) {
            throw HttpProcessingException(404)
        }

        val decks =
            deckNames.mapNotNull {
                deckInterface.findDeck(player, it)
            }.toMutableList()

        if (tournament.handler.canJoinTournament(tournament, player) && tournament.handler.canRegisterDecks(tournament, player, decks)) {
            repository.addPlayer(tournamentId, player)
            val deckMap = mutableMapOf<String, GameDeck>()
            tournament.handler.getRegisterDeckTypes(tournament).forEachIndexed { index, type ->
                val deck = decks[index]
                deckMap[type] = deck
                repository.upsertDeck(tournamentId, player, type, deck.name, deck.notes, deck.targetFormat, deck.toDeckString())
            }
            tournament.players.add(TournamentParticipant(player, deckMap))
        } else {
            throw HttpProcessingException(403)
        }
    }

    override fun leaveTournament(tournamentId: String, player: String) {
        val tournament = loadedTournaments[tournamentId]
        if (tournament == null || tournament.finished) {
            throw HttpProcessingException(404)
        }

        repository.setPlayerDrop(tournamentId, player, true)
        tournament.players.firstOrNull { it.player == player }?.dropped = true
    }

    override fun registerDecks(tournamentId: String, player: String, deckNames: List<String>) {
        val tournament = loadedTournaments[tournamentId]
        if (tournament == null || tournament.finished) {
            throw HttpProcessingException(404)
        }

        val decks =
            deckNames.mapNotNull {
                deckInterface.findDeck(player, it)
            }.toMutableList()

        val participant = tournament.players.firstOrNull { it.player == player }
        if (participant == null) {
            throw HttpProcessingException(404)
        }

        if (tournament.handler.canRegisterDecks(tournament, player, decks)) {
            tournament.handler.getRegisterDeckTypes(tournament).forEachIndexed { index, type ->
                val deck = decks[index]
                repository.upsertDeck(tournamentId, player, type, deck.name, deck.notes, deck.targetFormat, deck.toDeckString())
                participant.decks[type] = deck
            }
        } else {
            throw HttpProcessingException(403)
        }
    }

    override fun setPlayerDrop(tournamentId: String, player: String, drop: Boolean): Boolean {
        val tournament = loadedTournaments[tournamentId]
        if (tournament == null || tournament.finished) {
            return false
        }

        val tournamentPlayer = tournament.players.firstOrNull { it.player == player } ?: return false
        repository.setPlayerDrop(tournamentId, player, drop)
        tournamentPlayer.dropped = drop

        return true
    }

    override fun setPlayerDeck(
        tournamentId: String,
        player: String,
        deckType: String,
        deckName: String,
    ): Boolean {
        val tournament = loadedTournaments[tournamentId]
        if (tournament == null || tournament.finished) {
            return false
        }

        val tournamentPlayer = tournament.players.firstOrNull { it.player == player } ?: return false

        val deck = deckInterface.findDeck(player, deckName) ?: return false

        repository.upsertDeck(tournamentId, player, deckType, deck.name, deck.notes, deck.targetFormat, deck.toDeckString())
        tournamentPlayer.decks[deckType] = deck

        return true
    }

    override fun update() {
        if (!initialized) {
            initialize()
            initialized = true
        }
        val tournamentsToUnload = mutableSetOf<String>()
        loadedTournaments.forEach { (tournamentId, tournamentInfo) ->
            val handler = tournamentInfo.handler
            if (tournamentInfo.stage != FINISHED_STAGE) {
                handler.progressTournament(tournamentInfo, DefaultTournamentProgress(tournamentInfo))
            }
            if (tournamentInfo.stage == FINISHED_STAGE && tournamentInfo.startDate.isBefore(LocalDateTime.now().minusHours(tournamentLingerHours))) {
                handler.unloadTournament(tournamentInfo)
                tournamentsToUnload.add(tournamentId)
                removeTournamentState(tournamentInfo)
            }
        }
        tournamentsToUnload.forEach {
            loadedTournaments.remove(it)
        }
    }

    override fun gameCancelled(gameId: String) {
        loadedTournaments.values.forEach { tournament ->
            val gameEntry = tournament.runningGames[gameId]
            if (gameEntry != null) {
                tournament.runningGames.remove(gameId)
                // Restart the game
                val newGameId = gameContainerInterface.createNewGame(gameEntry.gameParticipants, gameEntry.gameSettings)
                tournament.runningGames[newGameId] = RunningGameEntry(gameEntry.round, gameEntry.gameSettings, gameEntry.gameParticipants)
            }
        }
    }

    override fun gameFinished(gameId: String, participants: Array<GameParticipant>, winner: String) {
        loadedTournaments.values.forEach { tournament ->
            val gameEntry = tournament.runningGames[gameId]
            if (gameEntry != null) {
                tournament.runningGames.remove(gameId)

                val playerOne = participants[0].playerId
                val playerTwo = participants[1].playerId
                repository.setMatchWinner(tournament.id, gameEntry.round, playerOne, playerTwo, winner)
                val match = tournament.matches.firstOrNull { it.round == gameEntry.round && it.playerOne == playerOne && it.playerTwo == playerTwo }
                match?.let {
                    it.winner = winner
                }
                updateTournamentState(tournament)
            }
        }
    }

    private fun initialize() {
        repository.getUnfinishedOrStartAfter(LocalDateTime.now().minusHours(tournamentLingerHours)).forEach { tournament ->
            val tournamentHandler = findHandler(tournament.type)

            addTournamentInternal(tournamentHandler, tournament)
        }
    }

    private fun addTournamentInternal(
        tournamentHandler: TournamentHandler<Any>,
        tournament: Tournament,
    ) {
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
                mutableMapOf(),
            )

        val tournamentDecks = repository.getTournamentDecks(tournament.tournamentId).groupBy { it.player }

        val tournamentPlayers = repository.getTournamentPlayers(tournament.tournamentId)
        tournamentPlayers.forEach {
            tournamentInfo.players.add(it.toParticipant(tournamentDecks[it.player].orEmpty()))
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

        createTournamentState(tournamentInfo)
    }

    private fun startMatch(
        round: Int,
        playerOne: String,
        playerTwo: String,
        tournament: DefaultTournamentInfo<Any>,
    ) {
        val handler = tournament.handler

        val type = handler.getPlayedDeckType(tournament, round)
        val deckOne = tournament.players.firstOrNull { it.player == playerOne }!!.decks[type]!!
        val deckTwo = tournament.players.firstOrNull { it.player == playerTwo }!!.decks[type]!!

        val gameSettings = handler.getGameSettings(tournament, round)
        val participants =
            arrayOf(
                GameParticipant(playerOne, deckOne),
                GameParticipant(playerTwo, deckTwo),
            )
        val gameId = gameContainerInterface.createNewGame(participants, gameSettings)
        tournament.runningGames[gameId] = RunningGameEntry(round, gameSettings, participants)
    }

    private fun TournamentPlayer.toParticipant(decks: List<TournamentDeck>): TournamentParticipant {
        return TournamentParticipant(
            player,
            decks.associate {
                it.type to GameDeck(it.name, it.notes, it.targetFormat, toDeckParts(it.contents))
            }.toMutableMap(),
            dropped,
        )
    }

    inner class DefaultTournamentProgress(
        private val info: DefaultTournamentInfo<Any>,
    ) : TournamentProgress {
        override fun updateState(round: Int, stage: String) {
            repository.setRoundAndStage(info.id, round, stage)
            info.stage = stage
            info.round = round
            updateTournamentState(info)
        }

        override fun createMatch(recipe: TournamentGameRecipe) {
            val playerOne = recipe.participants[0]
            val playerTwo = recipe.participants[1]
            val round = recipe.round

            repository.createMatch(info.id, round, playerOne, playerTwo)

            startMatch(round, playerOne, playerTwo, info)

            info.matches.add(TournamentMatch(recipe.round, playerOne, playerTwo, null))
            updateTournamentState(info)
        }

        override fun awardBye(round: Int, player: String) {
            repository.createMatch(info.id, round, player, BYE_NAME, player)

            info.matches.add(TournamentMatch(round, player, BYE_NAME, player))
            updateTournamentState(info)
        }

        override fun dropPlayer(player: String) {
            repository.setPlayerDrop(info.id, player, true)
            info.players.firstOrNull { it.player == player }?.dropped = true
            updateTournamentState(info)
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
        val runningGames: MutableMap<String, RunningGameEntry>,
    ) : TournamentInfo<TournamentData>, TournamentClientInfo {
        override val status: String
            get() = handler.getTournamentStatus(this)
        override val finished: Boolean
            get() = stage == FINISHED_STAGE
    }

    data class RunningGameEntry(
        val round: Int,
        val gameSettings: GameSettings,
        val gameParticipants: Array<GameParticipant>,
    )
}
