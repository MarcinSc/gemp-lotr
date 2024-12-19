package org.ccgemp.server.player

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

data class Player(
    val id: Int = 0,
    val name: String,
    val password: String,
    val email: String?,
    val type: String,
    val lastLoginReword: Int?,
    val bannedUntil: Long?,
    val createIp: String?,
    val lastIp: String?,
) {
    fun hasRole(role: String): Boolean = type.contains(role, true)
    fun bannedToLocalDateTime(): LocalDateTime? {
        return bannedUntil?.let {
            Instant.ofEpochMilli(it).atZone(UTC).toLocalDateTime()
        }
    }
}
