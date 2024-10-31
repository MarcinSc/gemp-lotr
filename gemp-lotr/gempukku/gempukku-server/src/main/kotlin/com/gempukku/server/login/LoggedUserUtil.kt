package com.gempukku.server.login

import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest

fun getLoggedUser(
    loggedUserInterface: LoggedUserInterface,
    request: HttpRequest,
): LoggedUser = loggedUserInterface.findLoggedUser(request) ?: throw HttpProcessingException(401)

fun getActingAsUser(
    loggedUserInterface: LoggedUserInterface,
    request: HttpRequest,
    adminRole: String,
    otherUser: String?,
): LoggedUser {
    val loggedUser = getLoggedUser(loggedUserInterface, request)
    return if (otherUser != null && loggedUser.roles.contains(adminRole)) {
        LoggedUser(otherUser, loggedUser.roles, loggedUser.lastAccess)
    } else {
        loggedUser
    }
}
