package com.gempukku.server.login

data class LoggedUser(
    val playerId: String,
    val roles: Set<String>,
    var lastAccess: Long,
)
