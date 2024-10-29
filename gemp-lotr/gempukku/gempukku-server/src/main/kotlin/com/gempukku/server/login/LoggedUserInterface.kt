package com.gempukku.server.login

import com.gempukku.server.HttpRequest
import com.gempukku.server.ResponseWriter

interface LoggedUserInterface {
    fun findLoggedUser(request: HttpRequest): LoggedUser?
    fun sendLogUserResponse(userId: String, responseWriter: ResponseWriter)
}