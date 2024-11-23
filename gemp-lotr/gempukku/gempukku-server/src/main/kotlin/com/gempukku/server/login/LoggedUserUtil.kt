package com.gempukku.server.login

import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.ResponseWriter
import com.gempukku.server.ServerRequestHandler

fun getLoggedUser(loggedUserInterface: LoggedUserInterface, request: HttpRequest): LoggedUser = loggedUserInterface.findLoggedUser(request) ?: throw HttpProcessingException(401)

fun getActingAsUser(
    loggedUserInterface: LoggedUserInterface,
    request: HttpRequest,
    adminRole: String,
    actAsParameter: String,
): LoggedUser {
    val loggedUser = getLoggedUser(loggedUserInterface, request)
    val otherUser = request.getParameter(actAsParameter)
    return if (otherUser != null && loggedUser.roles.contains(adminRole)) {
        LoggedUser(otherUser, loggedUser.roles, loggedUser.lastAccess)
    } else {
        loggedUser
    }
}

fun validateHasRole(
    requestHandler: ServerRequestHandler,
    loggedUserInterface: LoggedUserInterface,
    role: String,
): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
    { request, responseWriter ->
        val loggedUser = getLoggedUser(loggedUserInterface, request)
        if (loggedUser.roles.contains(role)) {
            throw HttpProcessingException(403)
        }

        requestHandler.handleRequest(request, responseWriter)
    }
