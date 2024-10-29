package com.gempukku.server.login

interface UserRolesProvider {
    fun getUserRoles(userId: String): Set<String>
}
