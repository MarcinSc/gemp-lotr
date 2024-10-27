package org.ccgemp.server.player.admin

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.server.player.PlayerDAO

val DAY_IN_MILIS: Long = 1000 * 60 * 60 * 24

@Exposes(AdminPlayerInterface::class)
class AdminPlayerSystem : AdminPlayerInterface {
    @Inject
    private lateinit var playerDao: PlayerDAO

    override fun banPlayer(login: String): Boolean {
        val player = playerDao.findPlayerByLogin(login)
        return if (player != null) {
            playerDao.banPlayer(player)
            true
        } else {
            false
        }
    }

    override fun banPlayers(logins: Array<String>): Boolean {
        val players = logins.mapNotNull { playerDao.findPlayerByLogin(it) }
        return if (players.size == logins.size) {
            playerDao.banPlayers(players)
            true
        } else {
            false
        }
    }

    override fun banPlayerTemporarily(login: String, days: Int): Boolean {
        val player = playerDao.findPlayerByLogin(login)
        return if (player != null) {
            playerDao.banPlayerTemporarily(player, System.currentTimeMillis() + days * DAY_IN_MILIS)
            true
        } else {
            false
        }
    }

    override fun unbanPlayer(login: String): Boolean {
        val player = playerDao.findPlayerByLogin(login)
        return if (player != null) {
            playerDao.unbanPlayer(player)
            true
        } else {
            false
        }
    }

    override fun getPlayerRoles(login: String): String? {
        val player = playerDao.findPlayerByLogin(login)
        return player?.type
    }

    override fun setPlayerRoles(login: String, roles: String): Boolean {
        val player = playerDao.findPlayerByLogin(login)
        return if (player != null) {
            playerDao.setPlayerType(player, roles)
            true
        } else {
            false
        }
    }
}