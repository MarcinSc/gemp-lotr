package com.gempukku.server.login

import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.ResponseWriter
import com.gempukku.server.ServerRequestHandler

fun getLoggedUser(loggedUserInterface: LoggedUserInterface, request: HttpRequest): LoggedUser = loggedUserInterface.findLoggedUser(request) ?: throw HttpProcessingException(401)

fun getActingAsUser(
    loggedUserInterface: LoggedUserInterface,
    userRolesProvider: UserRolesProvider,
    request: HttpRequest,
    adminRole: String,
    actAsParameter: String,
): LoggedUser {
    val loggedUser = getLoggedUser(loggedUserInterface, request)
    val otherUser = request.getParameter(actAsParameter)
    return if (otherUser != null && userRolesProvider.getUserRoles(loggedUser.userId).contains(adminRole)) {
        LoggedUser(otherUser, loggedUser.lastAccess)
    } else {
        loggedUser
    }
}

fun validateHasRole(requestHandler: ServerRequestHandler, loggedUserInterface: LoggedUserInterface, userRolesProvider: UserRolesProvider, role: String): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
    { request, responseWriter ->
        val loggedUser = getLoggedUser(loggedUserInterface, request)
        if (userRolesProvider.getUserRoles(loggedUser.userId).contains(role)) {
            throw HttpProcessingException(403)
        }

        requestHandler.handleRequest(request, responseWriter)
    }
