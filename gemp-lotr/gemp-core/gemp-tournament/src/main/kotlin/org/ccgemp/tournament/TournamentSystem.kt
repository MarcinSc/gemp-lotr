package org.ccgemp.tournament

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.server.HttpProcessingException
import org.ccgemp.deck.DeckInterface
import org.ccgemp.deck.GameDeck
import org.ccgemp.deck.toDeckParts
import org.ccgemp.deck.toDeckString
import org.ccgemp.game.GameContainerInterface
import org.ccgemp.game.GameParticipant
import java.time.LocalDateTime

const val FINISHED_STAGE = "FINISHED"

@Exposes(UpdatedSystem::class, TournamentInterface::class)
class TournamentSystem : TournamentInterface, UpdatedSystem, LifecycleObserver {
    @Inject
    private lateinit var repository: TournamentRepository

    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var gameContainerInterface: GameContainerInterface<*>

    @InjectValue(value = "tournament.linger.hours")
    private var tournamentLingerHours: Long = 12

    private var initialized: Boolean = false

    private val handlerMap = mutableMapOf<String, TournamentHandler<Any>>()

    private val loadedTournaments = mutableMapOf<String, DefaultTournamentInfo<Any>>()

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

    override fun joinTournament(
        tournamentId: String,
        player: String,
        deckNames: List<String>,
        forced: Boolean,
    ) {
        val tournament = loadedTournaments[tournamentId]
        if (tournament == null || tournament.finished) {
            throw HttpProcessingException(404)
        }

        val decks =
            deckNames.mapNotNull {
                deckInterface.findDeck(player, it)
            }.toMutableList()

        if (tournament.handler.canJoinTournament(tournament, player, forced) && tournament.handler.canRegisterDecks(tournament, player, decks, forced)) {
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

        repository.dropPlayer(tournamentId, player)
        tournament.players.firstOrNull { it.player == player }?.dropped = true
    }

    override fun registerDecks(
        tournamentId: String,
        player: String,
        deckNames: List<String>,
        forced: Boolean,
    ) {
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

        if (tournament.handler.canRegisterDecks(tournament, player, decks, forced)) {
            tournament.handler.getRegisterDeckTypes(tournament).forEachIndexed { index, type ->
                val deck = decks[index]
                repository.upsertDeck(tournamentId, player, type, deck.name, deck.notes, deck.targetFormat, deck.toDeckString())
                participant.decks[type] = deck
            }
        } else {
            throw HttpProcessingException(403)
        }
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
            }
        }
        tournamentsToUnload.forEach {
            loadedTournaments.remove(it)
        }
    }

    private fun initialize() {
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
        }
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
        val participants =
            arrayOf(
                GameParticipant(playerOne, deckOne),
                GameParticipant(playerTwo, deckTwo),
            )
        val gameId = gameContainerInterface.createNewGame(participants, handler.getGameSettings(tournament, round))
        tournament.runningGames.add(gameId)
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
        }

        override fun createMatch(recipe: TournamentGameRecipe) {
            val playerOne = recipe.participants[0]
            val playerTwo = recipe.participants[1]
            val round = recipe.round

            repository.createMatch(info.id, round, playerOne, playerTwo)

            startMatch(round, playerOne, playerTwo, info)

            info.matches.add(TournamentMatch(recipe.round, playerOne, playerTwo, null))
        }

        override fun awardBye(round: Int, player: String) {
            repository.createMatch(info.id, round, player, BYE_NAME, player)

            info.matches.add(TournamentMatch(round, player, BYE_NAME, player))
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
