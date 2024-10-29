package com.gempukku.server.chat

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.HttpRequest
import com.gempukku.server.ResponseWriter
import com.gempukku.server.login.LoggedUser
import com.gempukku.server.login.LoggedUserInterface

@Exposes(LoggedUserInterface::class)
class SimpleLoggedUser : LoggedUserInterface {
    var loggedUser: LoggedUser? = null

    override fun findLoggedUser(request: HttpRequest): LoggedUser? {
        return loggedUser
    }

    override fun sendLogUserResponse(userId: String, responseWriter: ResponseWriter) {
    }
}