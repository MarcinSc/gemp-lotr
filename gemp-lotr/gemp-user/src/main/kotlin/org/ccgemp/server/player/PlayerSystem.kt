package org.ccgemp.server.player

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectProperty
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.generateUniqueId
import com.gempukku.server.login.LoggedUserInterface
import java.security.MessageDigest

@Exposes(PlayerInterface::class)
class PlayerSystem : PlayerInterface {
    @Inject
    private lateinit var playerDao: PlayerDAO

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject(allowsNull = true)
    private var playerManagementCommunication: PlayerManagementCommunication? = null

    @InjectProperty("default.roles")
    private lateinit var defaultRoles: String

    private val validLoginChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
    private val invalidLoginPrefixes = setOf("admin", "guest", "system", "bye")

    override fun register(login: String, password: String, email: String, remoteIp: String): String? {
        if (!validLoginName(login))
            return null
        return if (playerDao.registerPlayer(login, encodePassword(password), email, defaultRoles, remoteIp)) {
            loggedUserInterface.logUser(login, defaultRoles.convertToRoleSet())
        } else {
            null
        }
    }

    override fun login(login: String, password: String, remoteIp: String): String? {
        val player = playerDao.loginPlayer(login, password)
        return if (player != null) {
            if ((player.bannedUntil == null) || (player.bannedUntil < System.currentTimeMillis())) {
                playerDao.updateLastIp(player, remoteIp)
                loggedUserInterface.logUser(login, player.type.convertToRoleSet())
            } else {
                throw PlayerBannedException()
            }
        } else {
            null
        }
    }

    override fun resetPassword(email: String) {
        val player = playerDao.findPlayerByEmail(email)
        if (player != null) {
            val resetToken = generateUniqueId()
            playerDao.updateForPasswordReset(player, resetToken)
            playerManagementCommunication?.sendPasswordResetEmail(email, resetToken)
        }
    }

    override fun resetPasswordValidate(password: String, resetToken: String): String? {
        val player = playerDao.findPlayerByPasswordResetToken(resetToken)
        return if (player != null) {
            playerDao.setPassword(player, encodePassword(password))
            player.email
        } else {
            null
        }
    }

    override fun changeEmail(login: String, password: String, newEmail: String): Boolean {
        val player = playerDao.loginPlayer(login, password)
        return if (player != null) {
            val changeEmailToken = generateUniqueId()
            playerDao.updateForEmailChange(player, newEmail, changeEmailToken)
            playerManagementCommunication?.sendEmailChangeEmail(newEmail, changeEmailToken)
            true
        } else {
            false
        }
    }

    override fun changeEmailValidate(changeEmailToken: String): Boolean {
        return playerDao.emailUpdateValidated(changeEmailToken)
    }

    private fun encodePassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.reset()
        return convertToHexString(digest.digest(password.toByteArray(charset("UTF-8"))))
    }

    private fun convertToHexString(bytes: ByteArray): String {
        val hexString = StringBuilder()
        for (aByte in bytes) {
            val hex = Integer.toHexString(0xFF and aByte.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }

    private fun validLoginName(login: String): Boolean {
        if (login.length < 2 || login.length > 30) throw LoginInvalidException()
        for (element in login) {
            if (!validLoginChars.contains("" + element)) throw LoginInvalidException()
        }

        val startsWithInvalidPrefix = invalidLoginPrefixes.any {
            login.startsWith(it, true)
        }

        return startsWithInvalidPrefix.not()
    }
}
