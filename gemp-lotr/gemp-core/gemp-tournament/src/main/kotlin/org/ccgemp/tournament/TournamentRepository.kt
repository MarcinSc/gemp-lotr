package org.ccgemp.tournament

interface TournamentRepository {
    fun getUnfinishedTournaments(): List<Tournament>
}
