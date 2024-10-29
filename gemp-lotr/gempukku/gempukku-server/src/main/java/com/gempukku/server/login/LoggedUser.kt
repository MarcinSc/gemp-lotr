package com.gempukku.server.login

data class LoggedUser(
    val userId: String,
    val roles: Set<String>,
    val lastAccess: Long,
)
