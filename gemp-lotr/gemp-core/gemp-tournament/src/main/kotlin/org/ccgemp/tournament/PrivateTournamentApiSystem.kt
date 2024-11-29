package org.ccgemp.tournament

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.AuthorizedApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.ResponseWriter
import org.ccgemp.common.splitText
import java.util.regex.Pattern

class PrivateTournamentApiSystem : AuthorizedApiSystem() {
    @Inject
    private lateinit var tournamentInterface: TournamentInterface

    @InjectValue("server.tournament.urlPrefix")
    private lateinit var urlPrefix: String

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
                getActingAsUser(request)

            tournamentInterface.joinTournament(tournamentId, actAsUser.userId, deckName?.splitText('\n').orEmpty())

            responseWriter.writeXmlResponse(null)
        }

    private fun executeLeaveTournament(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/leave$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)

            val actAsUser =
                getActingAsUser(request)

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
                getActingAsUser(request)

            tournamentInterface.registerDecks(tournamentId, actAsUser.userId, deckName.splitText('\n'))

            responseWriter.writeXmlResponse(null)
        }
}
