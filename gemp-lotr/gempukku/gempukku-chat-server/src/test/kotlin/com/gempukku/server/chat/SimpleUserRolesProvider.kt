package com.gempukku.server.chat

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.login.UserRolesProvider

@Exposes(UserRolesProvider::class)
class SimpleUserRolesProvider : UserRolesProvider {
    override fun getUserRoles(userId: String): Set<String> {
        return emptySet()
    }
}
