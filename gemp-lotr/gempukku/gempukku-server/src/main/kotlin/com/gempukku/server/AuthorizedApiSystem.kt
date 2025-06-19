package com.gempukku.server

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.login.LoggedUser
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.UserRolesProvider

abstract class AuthorizedApiSystem : ApiSystem() {
    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

    @InjectValue("roles.admin")
    private lateinit var adminRole: String

    @InjectValue("parameterNames.actAsParameter")
    private lateinit var actAsParameter: String

    protected fun getActingAsUser(request: HttpRequest): LoggedUser {
        val loggedUser = getLoggedUser(request)
        val otherUser = request.getParameter(actAsParameter)
        return if (otherUser != null && userRolesProvider.getUserRoles(loggedUser.userId).contains(adminRole)) {
            LoggedUser(otherUser, loggedUser.lastAccess)
        } else {
            loggedUser
        }
    }

    protected fun isAdmin(request: HttpRequest): Boolean {
        val loggedUser = getLoggedUser(request)
        return userRolesProvider.getUserRoles(loggedUser.userId).contains(adminRole)
    }

    protected fun validateIsAdmin(requestHandler: ServerRequestHandler): ServerRequestHandler = validateHasRole(adminRole, requestHandler)

    protected fun validateHasRole(role: String, requestHandler: ServerRequestHandler): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val loggedUser = getLoggedUser(request)
            if (userRolesProvider.getUserRoles(loggedUser.userId).contains(role)) {
                throw HttpProcessingException(403)
            }

            requestHandler.handleRequest(request, responseWriter)
        }

    private fun getLoggedUser(request: HttpRequest): LoggedUser {
        return loggedUserInterface.findLoggedUser(request) ?: throw HttpProcessingException(401)
    }
}
