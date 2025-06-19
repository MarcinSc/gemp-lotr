package org.ccgemp.game

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.AuthorizedApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.ServerRequestHandler
import com.gempukku.server.polling.LongPolling
import java.util.regex.Pattern

class GameApiSystem<GameEvent, ObserveSettings> : AuthorizedApiSystem() {
    @InjectValue("server.game.urlPrefix")
    private lateinit var urlPrefix: String

    @Inject
    private lateinit var longPolling: LongPolling

    @Inject
    private lateinit var gameContainerInterface: GameContainerInterface<GameEvent, ObserveSettings>

    @Inject
    private lateinit var gameEventSinkProducer: GameEventSinkProducer<GameEvent>

    @Inject(allowsNull = true)
    private var observeSettingsExtractor: GameObserveSettingsExtractor<ObserveSettings>? = null

    @InjectValue("parameterNames.pollId")
    private lateinit var pollIdParameterName: String

    override fun registerAPIs(): List<Registration> {
        return listOf(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/([^/]*)$",
                executeGetGame(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/([^/]*)$",
                executePostGame(),
            ),
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/([^/]*)/cardInfo$",
                executeGetCardInfo(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/([^/]*)/concede",
                executeConcede(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/([^/]*)/cancel",
                executeCancel(),
            ),
        )
    }

    private fun executeGetGame(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val gameId = request.uri.substring(urlPrefix.length + 1)
            val actAsUser =
                getActingAsUser(request)
            val gatheringGameStream = GatheringGameStream<GameEvent>()
            val added =
                gameContainerInterface.joinGame(
                    gameId,
                    actAsUser.userId,
                    isAdmin(request),
                    gatheringGameStream,
                )
            if (added == null) {
                throw HttpProcessingException(404)
            }

            observeSettingsExtractor?.let {
                val observeSettings = it.extractSettings(request)
                gameContainerInterface.setPlayerObserveSettings(gameId, actAsUser.userId, observeSettings)
            }

            val pollId = longPolling.registerLongPoll(gatheringGameStream.gatheringStream, added)
            longPolling.registerSink(
                pollId,
                gameEventSinkProducer.createEventSink(gameId, pollId, responseWriter),
            )
        }

    private fun executePostGame(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val gameId = request.uri.substring(urlPrefix.length + 1)
            val actAsUser =
                getActingAsUser(request)

            val decisionId = request.getParameter("decisionId")
            val decisionValue = request.getParameter("decisionValue")

            observeSettingsExtractor?.let {
                val observeSettings = it.extractSettings(request)
                gameContainerInterface.setPlayerObserveSettings(gameId, actAsUser.userId, observeSettings)
            }

            if (decisionId != null && decisionValue != null) {
                gameContainerInterface.processPlayerDecision(gameId, actAsUser.userId, decisionId, decisionValue)
            }

            val pollId = request.getParameter(pollIdParameterName) ?: throw HttpProcessingException(404)
            val found =
                longPolling.registerSink(
                    pollId,
                    gameEventSinkProducer.createEventSink(gameId, pollId, responseWriter),
                )
            if (!found) {
                throw HttpProcessingException(404)
            }
        }

    private fun executeGetCardInfo(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/cardInfo$")
            val matcher = pattern.matcher(request.uri)
            val gameId = matcher.group(1)

            val cardId = request.getParameter("cardId") ?: throw HttpProcessingException(400)

            val actAsUser = getActingAsUser(request)

            gameContainerInterface.produceCardInfo(gameId, actAsUser.userId, cardId, responseWriter)
        }

    private fun executeConcede(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/concede")
            val matcher = pattern.matcher(request.uri)
            val gameId = matcher.group(1)

            val actAsUser = getActingAsUser(request)

            gameContainerInterface.concede(gameId, actAsUser.userId)

            responseWriter.writeXmlResponse(null)
        }

    private fun executeCancel(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val pattern = Pattern.compile("^$urlPrefix/([^/]*)/cancel")
            val matcher = pattern.matcher(request.uri)
            val gameId = matcher.group(1)

            val actAsUser = getActingAsUser(request)

            gameContainerInterface.cancel(gameId, actAsUser.userId)

            responseWriter.writeXmlResponse(null)
        }
}
