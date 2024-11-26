package com.gempukku.server.chat

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.ApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.ResponseWriter
import com.gempukku.server.chat.polling.GatheringChatStream
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.UserRolesProvider
import com.gempukku.server.login.getActingAsUser
import com.gempukku.server.polling.LongPolling

class ChatApiSystem : ApiSystem() {
    @Inject
    private lateinit var chat: ChatInterface

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

    @Inject
    private lateinit var longPolling: LongPolling

    @Inject
    private lateinit var chatEventSinkProducer: ChatEventSinkProducer

    @InjectValue("server.chat.urlPrefix")
    private lateinit var urlPrefix: String

    @InjectValue("roles.admin")
    private lateinit var adminRole: String

    @InjectValue("parameterNames.actAsParameter")
    private lateinit var actAsParameter: String

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
                getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val gatheringChatStream = GatheringChatStream()
            val added =
                chat.joinUser(
                    roomName,
                    actAsUser.userId,
                    userRolesProvider.getUserRoles(actAsUser.userId).contains(adminRole),
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
                getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val message = request.getParameter("message")

            if (message != null && message.trim().isNotEmpty()) {
                chat.sendMessage(roomName, actAsUser.userId, message, userRolesProvider.getUserRoles(actAsUser.userId).contains(adminRole))
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
