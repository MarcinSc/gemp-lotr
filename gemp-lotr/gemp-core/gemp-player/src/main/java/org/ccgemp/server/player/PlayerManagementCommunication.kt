package org.ccgemp.server.player

interface PlayerManagementCommunication {
    fun sendValidateRegistrationEmail(
        email: String,
        validateEmailToken: String,
    )

    fun sendPasswordResetEmail(
        email: String,
        resetToken: String,
    )

    fun sendEmailChangeEmail(
        newEmail: String,
        changeEmailToken: String,
    )
}
