package org.ccgemp.tournament

import org.ccgemp.game.GameDeck
import org.ccgemp.game.GameSettings

interface TournamentHandler<TournamentData> {
    fun initializeTournament(tournament: Tournament): TournamentData

    fun getPlayerDeck(tournament: TournamentInfo<TournamentData>, player: String, round: Int): GameDeck

    fun getGameSettings(tournament: TournamentInfo<TournamentData>, round: Int): GameSettings

    fun progressTournament(tournament: TournamentInfo<TournamentData>, tournamentProgress: TournamentProgress)

    fun getTournamentStatus(tournament: TournamentInfo<TournamentData>): String

    fun unloadTournament(tournament: TournamentInfo<TournamentData>)
}
