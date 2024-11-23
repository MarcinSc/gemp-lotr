package org.ccgemp.server.player

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.generateUniqueId
import com.gempukku.server.login.UserRolesProvider
import java.security.MessageDigest

@Exposes(PlayerInterface::class, UserRolesProvider::class)
class PlayerSystem : PlayerInterface, UserRolesProvider {
    @Inject
    private lateinit var playerRepository: PlayerRepository

    @Inject(allowsNull = true)
    private var playerManagementCommunication: PlayerManagementCommunication? = null

    @InjectValue("roles.default")
    private lateinit var defaultRoles: String

    private val validLoginChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
    private val invalidLoginPrefixes = setOf("admin", "guest", "system", "bye")

    override fun register(
        login: String,
        password: String,
        email: String,
        remoteIp: String,
    ): Boolean {
        if (!validLoginName(login)) {
            return false
        }
        val validateEmailToken = generateUniqueId()

        val success =
            playerRepository.registerPlayer(
                login,
                encodePassword(password),
                email,
                validateEmailToken,
                defaultRoles,
                remoteIp,
            )
        if (success) {
            playerManagementCommunication?.sendValidateRegistrationEmail(email, validateEmailToken)
        }
        return success
    }

    override fun login(login: String, password: String, remoteIp: String): Boolean {
        val player = playerRepository.loginPlayer(login, password)
        return if (player != null) {
            if ((player.bannedUntil == null) || (player.bannedUntil < System.currentTimeMillis())) {
                playerRepository.updateLastIp(player, remoteIp)
                true
            } else {
                throw PlayerBannedException()
            }
        } else {
            false
        }
    }

    override fun resetPassword(email: String) {
        val player = playerRepository.findPlayerByEmail(email)
        if (player != null) {
            val resetToken = generateUniqueId()
            playerRepository.updateForPasswordReset(player, resetToken)
            playerManagementCommunication?.sendPasswordResetEmail(email, resetToken)
        }
    }

    override fun resetPasswordValidate(password: String, resetToken: String): String? {
        val player = playerRepository.findPlayerByPasswordResetToken(resetToken)
        return if (player != null) {
            playerRepository.setPassword(player, encodePassword(password))
            player.email
        } else {
            null
        }
    }

    override fun changeEmail(login: String, password: String, newEmail: String): Boolean {
        val player = playerRepository.loginPlayer(login, password)
        return if (player != null) {
            val changeEmailToken = generateUniqueId()
            playerRepository.updateForEmailChange(player, newEmail, changeEmailToken)
            playerManagementCommunication?.sendEmailChangeEmail(newEmail, changeEmailToken)
            true
        } else {
            false
        }
    }

    override fun changeEmailValidate(changeEmailToken: String) {
        playerRepository.findPlayerByChangeEmailToken(changeEmailToken)?.let {
            playerRepository.confirmEmailUpdate(it)
        }
    }

    override fun findPlayerByLogin(login: String): Player? = playerRepository.findPlayerByLogin(login)

    override fun getUserRoles(userId: String): Set<String> =
        findPlayerByLogin(userId)
            ?.let {
                it.type.convertToRoleSet()
            }.orEmpty()

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

        val startsWithInvalidPrefix =
            invalidLoginPrefixes.any {
                login.startsWith(it, true)
            }

        return startsWithInvalidPrefix.not()
    }
}
