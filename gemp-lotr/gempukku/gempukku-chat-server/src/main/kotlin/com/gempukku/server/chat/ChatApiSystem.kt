package com.gempukku.server.chat

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.AuthorizedApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.ResponseWriter
import com.gempukku.server.chat.polling.GatheringChatStream
import com.gempukku.server.polling.LongPolling

class ChatApiSystem : AuthorizedApiSystem() {
    @Inject
    private lateinit var chat: ChatInterface

    @Inject
    private lateinit var longPolling: LongPolling

    @Inject
    private lateinit var chatEventSinkProducer: ChatEventSinkProducer

    @InjectValue("server.chat.urlPrefix")
    private lateinit var urlPrefix: String

    @InjectValue("parameterNames.pollId")
    private lateinit var pollIdParameterName: String

    override fun registerAPIs(): List<Registration> {
        return listOf(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/.*$",
                executeGetChat(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/.*$",
                executePostChat(),
            ),
        )
    }

    private fun executeGetChat(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val roomName = request.uri.substring(urlPrefix.length + 1)
            val actAsUser =
                getActingAsUser(request)
            val gatheringChatStream = GatheringChatStream()
            val added =
                chat.joinUser(
                    roomName,
                    actAsUser.userId,
                    isAdmin(request),
                    gatheringChatStream,
                )
            if (added == null) {
                throw HttpProcessingException(404)
            }
            val pollId = longPolling.registerLongPoll(gatheringChatStream.gatheringStream, added)
            longPolling.registerSink(
                pollId,
                chatEventSinkProducer.createChatEventSink(roomName, pollId, responseWriter),
            )
        }

    private fun executePostChat(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val roomName = request.uri.substring(urlPrefix.length + 1)
            val actAsUser =
                getActingAsUser(request)
            val message = request.getParameter("message")

            if (message != null && message.trim().isNotEmpty()) {
                chat.sendMessage(roomName, actAsUser.userId, message, isAdmin(request))
            }

            val pollId = request.getParameter(pollIdParameterName) ?: throw HttpProcessingException(404)
            val found =
                longPolling.registerSink(
                    pollId,
                    chatEventSinkProducer.createChatEventSink(roomName, pollId, responseWriter),
                )
            if (!found) {
                throw HttpProcessingException(404)
            }
        }
}
