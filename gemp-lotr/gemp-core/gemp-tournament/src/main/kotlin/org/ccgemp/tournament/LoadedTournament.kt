package org.ccgemp.tournament

interface LoadedTournament {
    val finished: Boolean
    val handler: TournamentHandler
}
