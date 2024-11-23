package com.gempukku.server.chat

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServer
import com.gempukku.server.ResponseWriter
import com.gempukku.server.chat.polling.GatheringChatStream
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.UserRolesProvider
import com.gempukku.server.login.getActingAsUser
import com.gempukku.server.polling.LongPolling

@Exposes(LifecycleObserver::class)
class ChatApiSystem : LifecycleObserver {
    @Inject
    private lateinit var chat: ChatInterface

    @Inject
    private lateinit var server: HttpServer

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

    private val deregistration: MutableList<Runnable> = mutableListOf()

    override fun afterContextStartup() {
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/.*$",
                executeGetChat(),
            ),
        )
        deregistration.add(
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

    override fun beforeContextStopped() {
        deregistration.forEach {
            it.run()
        }
        deregistration.clear()
    }
}
