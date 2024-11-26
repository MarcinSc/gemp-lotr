package org.ccgemp.tournament

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.ApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.ResponseWriter
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.UserRolesProvider
import com.gempukku.server.login.getActingAsUser
import org.ccgemp.common.splitText
import java.util.regex.Pattern

class PrivateTournamentApiSystem : ApiSystem() {
    @Inject
    private lateinit var tournamentInterface: TournamentInterface

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

    @InjectValue("server.tournament.urlPrefix")
    private lateinit var urlPrefix: String

    @InjectValue("roles.admin")
    private lateinit var adminRole: String

    @InjectValue("parameterNames.actAsParameter")
    private lateinit var actAsParameter: String

    override fun registerAPIs(): List<Registration> {
        return listOf(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/([^/]*)/join$",
                executeJoinTournament(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/([^/]*)/leave",
                executeLeaveTournament(),
            ),
            server.registerRequestHandler(
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
                getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)

            tournamentInterface.joinTournament(tournamentId, actAsUser.userId, deckName?.splitText('\n').orEmpty())

            responseWriter.writeXmlResponse(null)
        }

    private fun executeLeaveTournament(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/leave$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)

            val actAsUser =
                getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)

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
                getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)

            tournamentInterface.registerDecks(tournamentId, actAsUser.userId, deckName.splitText('\n'))

            responseWriter.writeXmlResponse(null)
        }
}
