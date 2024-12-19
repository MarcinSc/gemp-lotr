package org.ccgemp.league

interface LeagueInterface {
    fun registerLeagueHandler(type: String, handler: LeagueHandler<Any>)

    fun getLiveLeagues(): List<LeagueClientInfo>
    fun getFinishedLeagues(): List<LeagueClientInfo>
}