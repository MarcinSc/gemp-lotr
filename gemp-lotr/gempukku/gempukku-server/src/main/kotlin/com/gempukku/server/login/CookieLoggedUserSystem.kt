package com.gempukku.server.login

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectProperty
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServer
import com.gempukku.server.ResponseWriter
import com.gempukku.server.generateUniqueId

@Exposes(LoggedUserInterface::class, UpdatedSystem::class)
class CookieLoggedUserSystem :
    UpdatedSystem,
    LoggedUserInterface {
    @InjectProperty("server.session.timeout")
    private var sessionTimeout: Long = 1000 * 60 * 10

    @Inject
    private lateinit var server: HttpServer

    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

    private val loggedUsersBySessionId: MutableMap<String, UserLastAccess> = mutableMapOf()

    override fun sendLogUserResponse(
        userId: String,
        responseWriter: ResponseWriter,
    ) {
        val lastAccess = UserLastAccess(userId, System.currentTimeMillis())

        var sessionId: String
        do {
            sessionId = generateUniqueId()
        } while (loggedUsersBySessionId.containsKey(sessionId))
        loggedUsersBySessionId[sessionId] = lastAccess

        responseWriter.writeXmlResponse(
            null,
            server.generateSetCookieHeader("loggedUser", sessionId),
        )
    }

    override fun findLoggedUser(request: HttpRequest): LoggedUser? {
        val sessionId = request.getCookie("loggedUser")
        return loggedUsersBySessionId[sessionId]?.let {
            it.lastAccess = System.currentTimeMillis()
            LoggedUser(it.userId, userRolesProvider.getUserRoles(it.userId), it.lastAccess)
        }
    }

    override fun update() {
        loggedUsersBySessionId.entries.removeAll {
            it.value.lastAccess + sessionTimeout < System.currentTimeMillis()
        }
    }
}

private data class UserLastAccess(
    val userId: String,
    var lastAccess: Long,
)
