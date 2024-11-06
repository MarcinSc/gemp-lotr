package org.ccgemp.tournament

interface TournamentRenderer {
    fun renderDecksHtml(tournament: TournamentClientInfo, player: String): String

    fun renderReportHtml(tournament: TournamentClientInfo): String

    fun renderInfoHtml(tournament: TournamentClientInfo): String
}
