package org.ccgemp.tournament

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
import com.gempukku.server.login.getActingAsUser
import org.ccgemp.common.splitText
import java.util.regex.Pattern

@Exposes(LifecycleObserver::class)
class PrivateTournamentApiSystem : LifecycleObserver {
    @Inject
    private lateinit var tournamentInterface: TournamentInterface

    @Inject
    private lateinit var httpServer: HttpServer

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @InjectValue("server.tournament.urlPrefix")
    private lateinit var urlPrefix: String

    @InjectValue("roles.admin")
    private lateinit var adminRole: String

    @InjectValue("parameterNames.actAsParameter")
    private lateinit var actAsParameter: String

    private val deregistration: MutableList<Runnable> = mutableListOf()

    override fun afterContextStartup() {
        deregistration.add(
            httpServer.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/([^/]*)/join$",
                executeJoinTournament(),
            ),
        )
        deregistration.add(
            httpServer.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/([^/]*)/leave",
                executeLeaveTournament(),
            ),
        )
        deregistration.add(
            httpServer.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/([^/]*)/registerdeck",
                executeRegisterDeck(),
            ),
        )
    }

    private fun executeJoinTournament(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/join$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)
            val deckName = request.getParameter("deckName")

            val actAsUser =
                getActingAsUser(loggedUserInterface, request, adminRole, actAsParameter)

            tournamentInterface.joinTournament(tournamentId, actAsUser.userId, deckName?.splitText('\n').orEmpty())

            responseWriter.writeXmlResponse(null)
        }

    private fun executeLeaveTournament(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/leave$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)

            val actAsUser =
                getActingAsUser(loggedUserInterface, request, adminRole, actAsParameter)

            tournamentInterface.leaveTournament(tournamentId, actAsUser.userId)

            responseWriter.writeXmlResponse(null)
        }

    private fun executeRegisterDeck(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/registerdeck$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            val actAsUser =
                getActingAsUser(loggedUserInterface, request, adminRole, actAsParameter)

            tournamentInterface.registerDecks(tournamentId, actAsUser.userId, deckName.splitText('\n'))

            responseWriter.writeXmlResponse(null)
        }

    override fun beforeContextStopped() {
        deregistration.forEach {
            it.run()
        }
        deregistration.clear()
    }
}
