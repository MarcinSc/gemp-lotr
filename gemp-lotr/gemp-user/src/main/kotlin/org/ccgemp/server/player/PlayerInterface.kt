package org.ccgemp.server.player

interface PlayerInterface {
    /**
     * Returns an authentication token if registered successfully
     */
    fun register(login: String, password: String, email: String, remoteIp: String): String?

    /**
     * Returns an authentication token if logged in successfully
     */
    fun login(login: String, password: String, remoteIp: String): String?

    fun resetPassword(email: String)

    fun resetPasswordValidate(password: String, resetToken: String): String?

    fun changeEmail(login: String, password: String, newEmail: String): Boolean

    fun changeEmailValidate(changeEmailToken: String)
}
