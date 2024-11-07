package org.ccgemp.tournament

import org.ccgemp.deck.GameDeck
import org.ccgemp.game.GameSettings

interface TournamentHandler<TournamentData> {
    fun initializeTournament(tournament: Tournament): TournamentData

    fun getGameSettings(tournament: TournamentInfo<TournamentData>, round: Int): GameSettings

    fun progressTournament(tournament: TournamentInfo<TournamentData>, tournamentProgress: TournamentProgress)

    fun getTournamentStatus(tournament: TournamentInfo<TournamentData>): String

    fun unloadTournament(tournament: TournamentInfo<TournamentData>)

    fun canJoinTournament(
        tournament: TournamentInfo<TournamentData>,
        player: String,
        forced: Boolean,
    ): Boolean

    fun canRegisterDecks(
        tournament: TournamentInfo<TournamentData>,
        player: String,
        decks: List<GameDeck>,
        forced: Boolean,
    ): Boolean

    fun getRegisterDeckTypes(
        tournament: TournamentInfo<TournamentData>,
    ): List<String>

    fun getPlayedDeckType(
        tournament: TournamentInfo<TournamentData>,
        round: Int,
    ): String
}
