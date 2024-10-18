package org.ccgemp.server.player

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.db.DbAccessInterface

@Exposes(PlayerDAO::class)
class DbPlayerDAO : PlayerDAO {
    @Inject
    private lateinit var dbAccess: DbAccessInterface

    override fun registerPlayer(
        login: String,
        password: String,
        email: String,
        type: String,
        remoteIp: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun loginPlayer(login: String, password: String): Player? {
        TODO("Not yet implemented")
    }

    override fun updateForPasswordReset(player: Player, resetToken: String) {
        TODO("Not yet implemented")
    }

    override fun setPassword(player: Player, password: String) {
        TODO("Not yet implemented")
    }

    override fun updateLastIp(player: Player, lastIp: String) {
        TODO("Not yet implemented")
    }

    override fun findPlayerByEmail(email: String): Player? {
        TODO("Not yet implemented")
    }

    override fun findPlayerByPasswordResetToken(resetToken: String): Player? {
        TODO("Not yet implemented")
    }

    override fun updateForEmailChange(player: Player, newEmail: String, changeEmailToken: String) {
        TODO("Not yet implemented")
    }

    override fun emailUpdateValidated(changeEmailToken: String): Boolean {
        TODO("Not yet implemented")
    }
}