package org.ccgemp.server.player.admin

interface AdminPlayerInterface {
    fun banPlayer(login: String): Boolean

    fun banPlayers(logins: Array<String>): Boolean

    fun banPlayerTemporarily(login: String, days: Int): Boolean

    fun unbanPlayer(login: String): Boolean

    fun getPlayerRoles(login: String): String?

    fun setPlayerRoles(login: String, roles: String): Boolean
}
