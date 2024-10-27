package org.ccgemp.server.player

interface PlayerManagementCommunication {
    fun sendPasswordResetEmail(email: String, resetToken: String)

    fun sendEmailChangeEmail(newEmail: String, changeEmailToken: String)
}