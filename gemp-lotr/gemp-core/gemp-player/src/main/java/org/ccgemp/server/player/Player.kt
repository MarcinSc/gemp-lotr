package org.ccgemp.server.player

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
}
