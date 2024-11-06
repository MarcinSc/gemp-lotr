package org.ccgemp.tournament

import org.ccgemp.deck.GameDeck
import org.ccgemp.game.GameSettings

interface TournamentHandler<TournamentData> {
    fun initializeTournament(tournament: Tournament): TournamentData

    fun getPlayerDeckIndex(tournament: TournamentInfo<TournamentData>, player: String, round: Int): Int

    fun getGameSettings(tournament: TournamentInfo<TournamentData>, round: Int): GameSettings

    fun progressTournament(tournament: TournamentInfo<TournamentData>, tournamentProgress: TournamentProgress)

    fun getTournamentStatus(tournament: TournamentInfo<TournamentData>): String

    fun unloadTournament(tournament: TournamentInfo<TournamentData>)

    fun canJoinTournament(
        tournament: TournamentInfo<TournamentData>,
        player: String,
        decks: List<GameDeck?>,
        forced: Boolean,
    ): Boolean

    fun canRegisterDeck(
        tournament: TournamentInfo<TournamentData>,
        player: String,
        deck: GameDeck,
        forced: Boolean,
    ): Boolean
}
