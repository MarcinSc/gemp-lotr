package com.gempukku.server.login

interface LoggedUserInterface {
    fun findLoggedUser(sessionId: String): LoggedUser?
    fun logUser(playerId: String, roles: Set<String>): String
}