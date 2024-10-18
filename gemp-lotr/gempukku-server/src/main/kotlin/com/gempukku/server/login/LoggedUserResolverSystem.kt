package com.gempukku.server.login

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.PeriodicallyUpdatedSystem
import com.gempukku.server.generateUniqueId

@Exposes(LoggedUserSystem::class)
class LoggedUserResolverSystem(
    private val sessionLength: Int = 1000 * 60 * 10,
    checkInterval: Int = 60
) :
    PeriodicallyUpdatedSystem(checkInterval), LoggedUserSystem {
    private val loggedUsersBySessionId: MutableMap<String, LoggedUser> = mutableMapOf()

    override fun logUser(playerId: String, roles: Set<String>): String {
        val loggedUser = LoggedUser(playerId, roles, System.currentTimeMillis())

        var sessionId: String
        do {
            sessionId = generateUniqueId()
        } while (loggedUsersBySessionId.containsKey(sessionId))
        loggedUsersBySessionId.put(sessionId, loggedUser)
        return sessionId
    }

    override fun findLoggedUser(sessionId: String): LoggedUser? {
        return loggedUsersBySessionId[sessionId]?.also {
            it.lastAccess = System.currentTimeMillis()
        }
    }

    override fun periodicUpdate() {
        loggedUsersBySessionId.entries.removeAll {
            it.value.lastAccess + sessionLength < System.currentTimeMillis()
        }
    }
}
