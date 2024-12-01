package org.ccgemp.tournament

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.ApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.ResponseWriter
import org.ccgemp.tournament.renderer.TournamentModelRenderer
import java.util.regex.Pattern

class PublicTournamentApiSystem : ApiSystem() {
    @Inject
    private lateinit var tournamentInterface: TournamentInterface

    @Inject
    private lateinit var tournamentModelRenderer: TournamentModelRenderer

    @InjectValue("server.tournament.urlPrefix")
    private lateinit var urlPrefix: String

    override fun registerAPIs(): List<Registration> {
        return listOf(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix$",
                executeGetCurrentTournaments(),
            ),
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/history$",
                executeGetHistoricTournaments(),
            ),
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/([^/]*)/deck/([^/]*)/html$",
                executeGetTournamentDecks(),
            ),
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/([^/]*)/report/html$",
                executeGetTournamentReport(),
            ),
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/([^/]*)$",
                executeGetTournamentInfo(),
            ),
        )
    }

    private fun executeGetCurrentTournaments(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val liveTournaments = tournamentInterface.getLiveTournaments()

            tournamentModelRenderer.renderGetTournaments(liveTournaments, responseWriter)
        }

    private fun executeGetHistoricTournaments(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val historicTournaments = tournamentInterface.getHistoricTournaments()

            tournamentModelRenderer.renderGetTournaments(historicTournaments, responseWriter)
        }

    private fun executeGetTournamentDecks(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/deck/([^/]*)/html$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)
            val player = matcher.group(2)
            val tournament = tournamentInterface.findTournament(tournamentId)
            if (tournament == null) {
                throw HttpProcessingException(404)
            }
            if (!tournament.finished) {
                throw HttpProcessingException(403)
            }
            val decks = tournament.players.firstOrNull { it.player == player } ?: throw HttpProcessingException(404)

            tournamentModelRenderer.renderGetTournamentDecks(player, decks.decks.values.toList(), responseWriter)
        }

    private fun executeGetTournamentReport(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/report/html$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)
            val tournament = tournamentInterface.findTournament(tournamentId)?.takeIf { it.finished } ?: throw HttpProcessingException(404)
            val standings = tournamentInterface.getStandings(tournamentId)

            tournamentModelRenderer.renderGetTournamentReport(tournament, standings, responseWriter)
        }

    private fun executeGetTournamentInfo(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)
            val tournament = tournamentInterface.findTournament(tournamentId) ?: throw HttpProcessingException(404)
            val standings = tournamentInterface.getStandings(tournamentId)

            tournamentModelRenderer.renderGetTournamentInfo(tournament, standings, responseWriter)
        }
}
