package com.gempukku.server.login

data class LoggedUser(
    val userId: String,
    val lastAccess: Long,
)
