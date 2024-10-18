package com.gempukku.server.login

import com.gempukku.context.processor.inject.InjectProperty
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.server.generateUniqueId

@Exposes(LoggedUserInterface::class, UpdatedSystem::class)
class LoggedUserSystem : UpdatedSystem, LoggedUserInterface {
    @InjectProperty("server.session.timeout")
    private var sessionTimeout: Long = 1000 * 60 * 10

    private val loggedUsersBySessionId: MutableMap<String, LoggedUser> = mutableMapOf()

    override fun logUser(playerId: String, roles: Set<String>): String {
        val loggedUser = LoggedUser(playerId, roles, System.currentTimeMillis())

        var sessionId: String
        do {
            sessionId = generateUniqueId()
        } while (loggedUsersBySessionId.containsKey(sessionId))
        loggedUsersBySessionId[sessionId] = loggedUser
        return sessionId
    }

    override fun findLoggedUser(sessionId: String): LoggedUser? {
        return loggedUsersBySessionId[sessionId]?.also {
            it.lastAccess = System.currentTimeMillis()
        }
    }

    override fun update() {
        loggedUsersBySessionId.entries.removeAll {
            it.value.lastAccess + sessionTimeout < System.currentTimeMillis()
        }
    }
}
