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
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(LifecycleObserver::class)
class TournamentApiSystem : LifecycleObserver {
    @Inject
    private lateinit var tournamentInterface: TournamentInterface

    @Inject
    private lateinit var server: HttpServer

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject
    private lateinit var tournamentRenderer: TournamentRenderer

    @InjectValue("server.tournament.urlPrefix")
    private lateinit var urlPrefix: String

    private val deregistration: MutableList<Runnable> = mutableListOf()

    private val minuteFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override fun afterContextStartup() {
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix$",
                executeGetCurrentTournaments(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/history$",
                executeGetHistoricTournaments(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/([^/]*)/deck/([^/]*)/html$",
                executeGetTournamentDecks(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/([^/]*)/report/html$",
                executeGetTournamentReport(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/([^/]*)$",
                executeGetTournamentInfo(),
            ),
        )
    }

    private fun executeGetCurrentTournaments(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { _, _, _, responseWriter ->
            val liveTournaments = tournamentInterface.getLiveTournaments()

            sendTournamentInfo(liveTournaments, responseWriter)
        }

    private fun executeGetHistoricTournaments(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { _, _, _, responseWriter ->
            val historicTournaments = tournamentInterface.getHistoricTournaments()

            sendTournamentInfo(historicTournaments, responseWriter)
        }

    private fun executeGetTournamentDecks(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { uri, _, _, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/deck/([^/]*)/html$")
            val matcher = pattern.matcher(uri)
            val tournamentId = matcher.group(1)
            val player = matcher.group(2)
            val tournament = tournamentInterface.getTournament(tournamentId)
            if (tournament == null) {
                throw HttpProcessingException(404)
            }
            if (!tournament.finished) {
                throw HttpProcessingException(403)
            }

            responseWriter.writeHtmlResponse(tournamentRenderer.renderDecksHtml(tournament, player))
        }

    private fun executeGetTournamentReport(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { uri, _, _, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/report/html$")
            val matcher = pattern.matcher(uri)
            val tournamentId = matcher.group(1)
            val tournament = tournamentInterface.getTournament(tournamentId)
            if (tournament == null) {
                throw HttpProcessingException(404)
            }
            if (!tournament.finished) {
                throw HttpProcessingException(403)
            }

            responseWriter.writeHtmlResponse(tournamentRenderer.renderReportHtml(tournament))
        }

    private fun executeGetTournamentInfo(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { uri, _, _, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)$")
            val matcher = pattern.matcher(uri)
            val tournamentId = matcher.group(1)
            val tournament = tournamentInterface.getTournament(tournamentId)
            if (tournament == null) {
                throw HttpProcessingException(404)
            }

            responseWriter.writeHtmlResponse(tournamentRenderer.renderInfoHtml(tournament))
        }

    private fun sendTournamentInfo(historicTournaments: List<TournamentClientInfo>, responseWriter: ResponseWriter) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()
        val tournaments = doc.createElement("tournaments")

        historicTournaments.forEach { tournament ->
            val tournamentElem = doc.createElement("tournament")
            tournamentElem.setAttribute("id", tournament.id)
            tournamentElem.setAttribute("name", tournament.name)
            // TODO: remove the unused attributes
            tournamentElem.setAttribute("format", "")
            tournamentElem.setAttribute("collection", "")
            tournamentElem.setAttribute("round", "")
            tournamentElem.setAttribute("startDate", minuteFormatter.format(tournament.startDate))
            // TODO: rename stage to status
            tournamentElem.setAttribute("stage", tournament.status)
            tournaments.appendChild(tournamentElem)
        }

        doc.appendChild(tournaments)

        responseWriter.writeXmlResponse(doc)
    }

    override fun beforeContextStopped() {
        deregistration.forEach {
            it.run()
        }
        deregistration.clear()
    }
}
