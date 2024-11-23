package org.ccgemp.tournament

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpServer
import com.gempukku.server.ServerRequestHandler
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.UserRolesProvider
import com.gempukku.server.login.validateHasRole
import org.ccgemp.json.JsonProvider
import org.ccgemp.tournament.composite.kickoff.ManualKickoff
import org.ccgemp.tournament.composite.pairing.ManualPairing
import org.ccgemp.tournament.composite.pairing.RoundPairing
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Exposes(LifecycleObserver::class)
class AdminTournamentApiSystem : LifecycleObserver {
    @Inject
    private lateinit var tournamentInterface: TournamentInterface

    @Inject
    private lateinit var httpServer: HttpServer

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

    @Inject
    private lateinit var manualKickoff: ManualKickoff

    @Inject
    private lateinit var manualPairing: ManualPairing

    @Inject
    private lateinit var jsonProvider: JsonProvider

    @InjectValue("roles.tournamentAdmin")
    private lateinit var adminRole: String

    @InjectValue("server.manualKickoff.urlPrefix")
    private lateinit var manualKickoffUrlPrefix: String

    @InjectValue("server.manualPairing.urlPrefix")
    private lateinit var manualPairingUrlPrefix: String

    @InjectValue("server.dropSwitch.urlPrefix")
    private lateinit var dropSwitchUrlPrefix: String

    @InjectValue("server.setPlayerDeck.urlPrefix")
    private lateinit var setPlayerDeckUrlPrefix: String

    @InjectValue("server.createTournament.urlPrefix")
    private lateinit var createTournamentUrlPrefix: String

    private val deregistration: MutableList<Runnable> = mutableListOf()

    override fun afterContextStartup() {
        deregistration.add(
            httpServer.registerRequestHandler(
                HttpMethod.POST,
                "^$manualKickoffUrlPrefix/([^/]*)/([^/]*)$",
                validateHasRole(executeManualKickoff(), loggedUserInterface, userRolesProvider, adminRole),
            ),
        )
        deregistration.add(
            httpServer.registerRequestHandler(
                HttpMethod.POST,
                "^$manualPairingUrlPrefix/([^/]*)/([^/]*)$",
                validateHasRole(executeManualPairing(), loggedUserInterface, userRolesProvider, adminRole),
            ),
        )
        deregistration.add(
            httpServer.registerRequestHandler(
                HttpMethod.POST,
                "^$dropSwitchUrlPrefix/([^/]*)$",
                validateHasRole(executeDropSwitch(), loggedUserInterface,userRolesProvider,  adminRole),
            ),
        )
        deregistration.add(
            httpServer.registerRequestHandler(
                HttpMethod.POST,
                "^$setPlayerDeckUrlPrefix/([^/]*)$",
                validateHasRole(executeSetPlayerDeck(), loggedUserInterface, userRolesProvider, adminRole),
            ),
        )
        deregistration.add(
            httpServer.registerRequestHandler(
                HttpMethod.POST,
                "^$createTournamentUrlPrefix/([^/]*)$",
                validateHasRole(executeCreateTournament(), loggedUserInterface, userRolesProvider, adminRole),
            ),
        )
    }

    private fun executeManualKickoff(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val pattern = Pattern.compile("^$manualKickoffUrlPrefix/([^/]*)/([^/]*)$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)
            val round = matcher.group(2).toInt()

            val result = manualKickoff.kickoffRound(tournamentId, round)
            responseWriter.writeJsonResponse("{\"success\":$result}")
        }

    private fun executeManualPairing(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val pattern = Pattern.compile("^$manualPairingUrlPrefix/([^/]*)/([^/]*)$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)
            val round = matcher.group(2).toInt()

            val pairing = request.getParameter("pairing") ?: throw HttpProcessingException(400)

            val roundPairing = jsonProvider.readJsonObject(pairing, RoundPairing::class.java)

            val result = manualPairing.pairRound(tournamentId, round, roundPairing)
            responseWriter.writeJsonResponse("{\"success\":$result}")
        }

    private fun executeDropSwitch(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val pattern = Pattern.compile("^$dropSwitchUrlPrefix/([^/]*)$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)

            val player = request.getParameter("player") ?: throw HttpProcessingException(400)
            val drop = request.getParameter("drop")?.toBoolean() ?: throw HttpProcessingException(400)

            val result = tournamentInterface.setPlayerDrop(tournamentId, player, drop)
            responseWriter.writeJsonResponse("{\"success\":$result}")
        }

    private fun executeSetPlayerDeck(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val pattern = Pattern.compile("^$setPlayerDeckUrlPrefix/([^/]*)$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)

            val player = request.getParameter("player") ?: throw HttpProcessingException(400)
            val deckType = request.getParameter("deckType") ?: throw HttpProcessingException(400)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            val result = tournamentInterface.setPlayerDeck(tournamentId, player, deckType, deckName)
            responseWriter.writeJsonResponse("{\"success\":$result}")
        }

    private fun executeCreateTournament(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val pattern = Pattern.compile("^$createTournamentUrlPrefix/([^/]*)$")
            val matcher = pattern.matcher(request.uri)
            val tournamentId = matcher.group(1)

            val type = request.getParameter("type") ?: throw HttpProcessingException(400)
            val name = request.getParameter("name") ?: throw HttpProcessingException(400)
            val startDateStr = request.getParameter("startDate") ?: throw HttpProcessingException(400)
            val parameters = request.getParameter("parameters") ?: throw HttpProcessingException(400)

            val startDate = LocalDateTime.parse(
                startDateStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            )

            val result = tournamentInterface.addTournament(tournamentId, type, name, startDate, parameters)
            responseWriter.writeJsonResponse("{\"success\":$result}")
        }

    override fun beforeContextStopped() {
        deregistration.forEach {
            it.run()
        }
        deregistration.clear()
    }
}
