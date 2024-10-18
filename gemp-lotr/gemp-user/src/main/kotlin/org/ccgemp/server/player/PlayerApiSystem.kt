package org.ccgemp.server.player

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectProperty
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServerSystem
import com.gempukku.server.ResponseWriter

@Exposes(LifecycleObserver::class)
class PlayerApiSystem : LifecycleObserver {
    @Inject
    private lateinit var server: HttpServerSystem

    @Inject
    private lateinit var playerInterface: PlayerInterface

    @InjectProperty("server.login.url")
    private lateinit var loginUrl: String

    @InjectProperty("server.register.url")
    private lateinit var registerUrl: String

    private val deregistration: MutableList<Runnable> = mutableListOf()

    override fun afterContextStartup() {
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST, "^$loginUrl$",
                executeLogin()
            )
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST, "^$registerUrl$",
                executeRegister()
            )
        )
    }

    private fun executeRegister(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { uri, request, remoteIp, responseWriter ->
            val login = request.getFormParameter("login")
            val password = request.getFormParameter("password")
            val email = request.getFormParameter("email")

            if (login == null || password == null || email == null) {
                throw HttpProcessingException(400)
            }
            try {
                val authenticationToken = playerInterface.register(login, password, email, remoteIp)
                authenticationToken?.let {
                    responseWriter.writeXmlResponse(
                        null,
                        server.generateSetCookieHeader("loggedUser", authenticationToken)
                    )
                } ?: throw HttpProcessingException(409)
            } catch (exp: LoginInvalidException) {
                throw HttpProcessingException(400)
            }
        }

    private fun executeLogin(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { uri, request, remoteIp, responseWriter ->
            val login = request.getFormParameter("login")
            val password = request.getFormParameter("password")

            if (login == null || password == null) {
                throw HttpProcessingException(400)
            }
            try {
                val authenticationToken = playerInterface.login(login, password, remoteIp)
                authenticationToken?.let {
                    responseWriter.writeXmlResponse(
                        null,
                        server.generateSetCookieHeader("loggedUser", authenticationToken)
                    )
                } ?: throw HttpProcessingException(403)
            } catch (exp: PlayerBannedException) {
                throw HttpProcessingException(409)
            }
        }

    override fun beforeContextStopped() {
        super.beforeContextStopped()
    }
}