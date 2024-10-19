package org.ccgemp.server.player

interface PlayerDAO {
    fun registerPlayer(login: String, password: String, email: String, type: String, remoteIp: String): Boolean

    fun loginPlayer(login: String, password: String): Player?

    fun updateForPasswordReset(player: Player, resetToken: String)

    fun setPassword(player: Player, password: String)

    fun updateLastIp(player: Player, lastIp: String)

    fun findPlayerByLogin(login: String): Player?

    fun findPlayerByEmail(email: String): Player?

    fun findPlayerByPasswordResetToken(resetToken: String): Player?

    fun updateForEmailChange(player: Player, newEmail: String, changeEmailToken: String)

    fun emailUpdateValidated(changeEmailToken: String)


    fun banPlayer(player: Player)

    fun banPlayers(players: List<Player>)

    fun banPlayerTemporarily(player: Player, bannedUntil: Long)

    fun unbanPlayer(player: Player)
}
