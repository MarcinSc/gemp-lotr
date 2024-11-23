package org.ccgemp.server.player.admin

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServer
import com.gempukku.server.ResponseWriter
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.UserRolesProvider
import com.gempukku.server.login.validateHasRole
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(LifecycleObserver::class)
class AdminPlayerApiSystem : LifecycleObserver {
    @Inject
    private lateinit var server: HttpServer

    @Inject
    private lateinit var playerInterface: AdminPlayerInterface

    @Inject
    private lateinit var loggedUserSystem: LoggedUserInterface
    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

    @InjectValue("server.banPlayer.url")
    private lateinit var banPlayerUrl: String

    @InjectValue("server.banPlayers.url")
    private lateinit var banPlayersUrl: String

    @InjectValue("server.banPlayerTemporarily.url")
    private lateinit var banPlayerTemporarilyUrl: String

    @InjectValue("server.unbanPlayer.url")
    private lateinit var unbanPlayerUrl: String

    @InjectValue("server.getPlayerRoles.url")
    private lateinit var getPlayerRolesUrl: String

    @InjectValue("server.setPlayerRoles.url")
    private lateinit var setPlayerRolesUrl: String

    @InjectValue("roles.admin")
    private lateinit var adminRole: String

    private val deregistration: MutableList<Runnable> = mutableListOf()

    override fun afterContextStartup() {
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$banPlayerUrl$",
                validateHasRole(executeBanPlayer(), loggedUserSystem,userRolesProvider,  adminRole),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$banPlayersUrl$",
                validateHasRole(executeBanPlayers(), loggedUserSystem,userRolesProvider,  adminRole),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$banPlayerTemporarilyUrl$",
                validateHasRole(executeBanPlayerTemporarily(), loggedUserSystem,userRolesProvider,  adminRole),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$unbanPlayerUrl",
                validateHasRole(executeUnbanPlayer(), loggedUserSystem,userRolesProvider,  adminRole),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$getPlayerRolesUrl$",
                validateHasRole(executeGetPlayerRoles(), loggedUserSystem,userRolesProvider,  adminRole),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$setPlayerRolesUrl",
                validateHasRole(executeSetPlayerRoles(), loggedUserSystem,userRolesProvider,  adminRole),
            ),
        )
    }

    private fun executeBanPlayer(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val login = request.getParameter("login") ?: throw HttpProcessingException(400)
            if (!playerInterface.banPlayer(login)) {
                throw HttpProcessingException(404)
            }
            responseWriter.writeXmlResponse(null)
        }

    private fun executeBanPlayers(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val logins = request.getParameters("login[]")
            if (logins.isEmpty()) {
                throw HttpProcessingException(400)
            }
            if (!playerInterface.banPlayers(logins.toTypedArray())) {
                throw HttpProcessingException(404)
            }
            responseWriter.writeXmlResponse(null)
        }

    private fun executeBanPlayerTemporarily(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val login = request.getParameter("login")
            val duration = request.getParameter("duration")?.toInt()
            if (login == null || duration == null) {
                throw HttpProcessingException(400)
            }
            if (!playerInterface.banPlayerTemporarily(login, duration)) {
                throw HttpProcessingException(404)
            }
            responseWriter.writeXmlResponse(null)
        }

    private fun executeUnbanPlayer(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val login = request.getParameter("login") ?: throw HttpProcessingException(400)
            if (!playerInterface.unbanPlayer(login)) {
                throw HttpProcessingException(404)
            }
            responseWriter.writeXmlResponse(null)
        }

    private fun executeGetPlayerRoles(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val login = request.getParameter("login") ?: throw HttpProcessingException(400)
            val playerRoles = playerInterface.getPlayerRoles(login) ?: throw HttpProcessingException(404)

            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val doc = documentBuilder.newDocument()
            val hasTester = doc.createElement("playerRoles")

            hasTester.setAttribute("result", playerRoles)

            responseWriter.writeXmlResponse(doc)
        }

    private fun executeSetPlayerRoles(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val login = request.getParameter("login") ?: throw HttpProcessingException(400)
            val playerRoles = request.getParameter("playerRoles") ?: throw HttpProcessingException(400)

            if (!playerInterface.setPlayerRoles(login, playerRoles)) {
                throw HttpProcessingException(404)
            }

            responseWriter.writeXmlResponse(null)
        }

    override fun beforeContextStopped() {
        deregistration.forEach {
            it.run()
        }
        deregistration.clear()
    }
}
