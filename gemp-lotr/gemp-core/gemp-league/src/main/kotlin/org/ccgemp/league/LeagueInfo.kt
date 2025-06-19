package org.ccgemp.league

interface LeagueInfo<LeagueData> {
    val id: String
    val stage: String
    val round: Int
    val players: List<String>
    val matches: List<LeagueMatch>
    val data: LeagueData
}