package org.ccgemp.league

interface LeagueHandler<LeagueData> {
    fun initializeLeague(league: League): LeagueData
    fun progressLeague(league: LeagueInfo<LeagueData>, leagueProgress: LeagueProgress)
    fun unloadLeague(league: LeagueInfo<LeagueData>)
}