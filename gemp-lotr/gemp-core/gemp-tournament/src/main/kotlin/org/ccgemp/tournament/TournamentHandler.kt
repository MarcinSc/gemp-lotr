package org.ccgemp.tournament

import org.ccgemp.common.GameDeck
import org.ccgemp.game.GameSettings
import org.ccgemp.tournament.composite.standing.PlayerStanding

interface TournamentHandler<TournamentData> {
    fun validateTournament(tournament: Tournament): Boolean

    fun initializeTournament(tournament: Tournament): TournamentData

    fun getGameSettings(tournament: TournamentInfo<TournamentData>, round: Int): GameSettings

    fun progressTournament(tournament: TournamentInfo<TournamentData>, tournamentProgress: TournamentProgress)

    fun getTournamentStatus(tournament: TournamentInfo<TournamentData>): String

    fun unloadTournament(tournament: TournamentInfo<TournamentData>)

    fun canJoinTournament(tournament: TournamentInfo<TournamentData>, player: String): Boolean

    fun canRegisterDecks(tournament: TournamentInfo<TournamentData>, player: String, decks: List<GameDeck>): Boolean

    fun getRegisterDeckTypes(tournament: TournamentInfo<TournamentData>): List<String>

    fun getPlayedDeckType(tournament: TournamentInfo<TournamentData>, round: Int): String

    fun getStandings(tournament: TournamentInfo<TournamentData>): List<PlayerStanding>
}
