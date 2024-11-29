package org.ccgemp.tournament.renderer

import com.gempukku.server.ResponseWriter
import org.ccgemp.common.GameDeck
import org.ccgemp.tournament.TournamentClientInfo

interface TournamentModelRenderer {
    fun renderGetTournaments(tournaments: List<TournamentClientInfo>, responseWriter: ResponseWriter)

    fun renderGetTournamentDecks(player: String, decks: List<GameDeck>, responseWriter: ResponseWriter)

    fun renderGetTournamentReport(tournament: TournamentClientInfo, responseWriter: ResponseWriter)

    fun renderGetTournamentInfo(tournament: TournamentClientInfo, responseWriter: ResponseWriter)
}
