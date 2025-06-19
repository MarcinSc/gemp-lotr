package org.ccgemp.tournament.renderer

import com.gempukku.server.ResponseWriter
import org.ccgemp.common.GameDeck
import org.ccgemp.tournament.TournamentClientInfo
import org.ccgemp.tournament.composite.standing.PlayerStanding

interface TournamentModelRenderer {
    fun renderGetTournaments(tournaments: List<TournamentClientInfo>, responseWriter: ResponseWriter)

    fun renderGetTournamentDecks(player: String, decks: List<GameDeck>, responseWriter: ResponseWriter)

    fun renderGetTournamentReport(tournament: TournamentClientInfo, standings: List<PlayerStanding>, responseWriter: ResponseWriter)

    fun renderGetTournamentInfo(tournament: TournamentClientInfo, standings: List<PlayerStanding>, responseWriter: ResponseWriter)
}
