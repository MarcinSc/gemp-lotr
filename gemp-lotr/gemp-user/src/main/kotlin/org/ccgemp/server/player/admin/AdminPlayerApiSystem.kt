package org.ccgemp.server.player.admin

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectProperty
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServerSystem
import com.gempukku.server.ResponseWriter
import com.gempukku.server.ServerRequestHandler
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.getLoggedUser

@Exposes(LifecycleObserver::class)
class AdminPlayerApiSystem : LifecycleObserver {
    @Inject
    private lateinit var server: HttpServerSystem

    @Inject
    private lateinit var playerInterface: AdminPlayerInterface

    @Inject
    private lateinit var loggedUserSystem: LoggedUserInterface

    @InjectProperty("server.banPlayer.url")
    private lateinit var banPlayerUrl: String

    @InjectProperty("server.banPlayers.url")
    private lateinit var banPlayersUrl: String

    @InjectProperty("server.banPlayerTemporarily.url")
    private lateinit var banPlayerTemporarilyUrl: String

    @InjectProperty("server.unbanPlayer.url")
    private lateinit var unbanPlayerUrl: String

    @InjectProperty("roles.admin")
    private lateinit var adminRole: String

    private val deregistration: MutableList<Runnable> = mutableListOf()

    override fun afterContextStartup() {
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST, "^$banPlayerUrl$",
                validateAdmin(executeBanPlayer())
            )
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST, "^$banPlayersUrl$",
                validateAdmin(executeBanPlayers())
            )
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST, "^$banPlayerTemporarilyUrl$",
                validateAdmin(executeBanPlayerTemporarily())
            )
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST, "^$unbanPlayerUrl",
                validateAdmin(executeUnbanPlayer())
            )
        )
    }

    private fun executeBanPlayer(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { _, request, _, responseWriter ->
            val login = request.getFormParameter("login") ?: throw HttpProcessingException(400)
            if (!playerInterface.banPlayer(login))
                throw HttpProcessingException(404)
            responseWriter.writeXmlResponse(null)
        }

    private fun executeBanPlayers(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { _, request, remoteIp, responseWriter ->
            val logins = request.getFormParameters("login[]")
            if (logins.isEmpty())
                throw HttpProcessingException(400)
            if (!playerInterface.banPlayers(logins.toTypedArray()))
                throw HttpProcessingException(404)
            responseWriter.writeXmlResponse(null)
        }

    private fun executeBanPlayerTemporarily(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { _, request, remoteIp, responseWriter ->
            val login = request.getFormParameter("login")
            val duration = request.getFormParameter("duration")?.toInt()
            if (login == null || duration == null)
                throw HttpProcessingException(400)
            if (!playerInterface.banPlayerTemporarily(login, duration))
                throw HttpProcessingException(404)
            responseWriter.writeXmlResponse(null)
        }

    private fun executeUnbanPlayer(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { _, request, _, responseWriter ->
            val login = request.getFormParameter("login") ?: throw HttpProcessingException(400)
            if (!playerInterface.unbanPlayer(login))
                throw HttpProcessingException(404)
            responseWriter.writeXmlResponse(null)
        }

    override fun beforeContextStopped() {
        deregistration.forEach {
            it.run()
        }
        deregistration.clear()
    }

    private fun validateAdmin(requestHandler: ServerRequestHandler): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { uri, request, remoteIp, responseWriter ->
            validateArmin(request)

            requestHandler.handleRequest(uri, request, remoteIp, responseWriter)
        }

    private fun validateArmin(request: HttpRequest) {
        val loggedUser = getLoggedUser(loggedUserSystem, request)
        if (loggedUser.roles.contains(adminRole))
            throw HttpProcessingException(403)
    }
}