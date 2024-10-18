package com.gempukku.server.login

import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest

fun findLoggedUser(loggedUserSystem: LoggedUserSystem, request: HttpRequest): LoggedUser? {
    return request.getCookie("loggedUser")?.let {
        loggedUserSystem.findLoggedUser(it)
    }
}

fun getLoggedUser(loggedUserSystem: LoggedUserSystem, request: HttpRequest): LoggedUser {
    return findLoggedUser(loggedUserSystem, request) ?: throw HttpProcessingException(401)
}

fun getActingAsUser(
    loggedUserSystem: LoggedUserSystem,
    request: HttpRequest,
    adminRole: String,
    otherUser: String?,
): LoggedUser {
    val loggedUser = getLoggedUser(loggedUserSystem, request)
    return if (otherUser != null && loggedUser.roles.contains(adminRole)) {
        LoggedUser(otherUser, loggedUser.roles, loggedUser.lastAccess)
    } else {
        loggedUser
    }
}