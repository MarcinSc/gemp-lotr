package org.ccgemp.server.player

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

@Exposes(LifecycleObserver::class)
class PlayerApiSystem : LifecycleObserver {
    @Inject
    private lateinit var server: HttpServer

    @Inject
    private lateinit var playerInterface: PlayerInterface

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @InjectValue("server.login.url")
    private lateinit var loginUrl: String

    @InjectValue("server.register.url")
    private lateinit var registerUrl: String

    @InjectValue("server.passwordReset.url")
    private lateinit var passwordResetUrl: String

    @InjectValue("server.passwordResetValidate.url")
    private lateinit var passwordResetValidateUrl: String

    @InjectValue("server.changeEmail.url")
    private lateinit var changeEmailUrl: String

    @InjectValue("server.changeEmailValidate.url")
    private lateinit var changeEmailValidateUrl: String

    private val deregistration: MutableList<Runnable> = mutableListOf()

    override fun afterContextStartup() {
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$loginUrl$",
                executeLogin(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$registerUrl$",
                executeRegister(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$passwordResetUrl$",
                executePasswordReset(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$passwordResetValidateUrl$",
                executePasswordResetValidate(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$changeEmailUrl$",
                executeChangeEmail(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$changeEmailValidateUrl$",
                executeChangeEmailValidate(),
            ),
        )
    }

    private fun executeRegister(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { _, request, remoteIp, responseWriter ->
            val login = request.getParameter("login")
            val password = request.getParameter("password")
            val email = request.getParameter("email")

            if (login == null || password == null || email == null) {
                throw HttpProcessingException(400)
            }
            try {
                val registered = playerInterface.register(login, password, email, remoteIp)
                if (registered) {
                    loggedUserInterface.sendLogUserResponse(login, responseWriter)
                } else {
                    throw HttpProcessingException(409)
                }
            } catch (exp: LoginInvalidException) {
                throw HttpProcessingException(400)
            }
        }

    private fun executeLogin(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { _, request, remoteIp, responseWriter ->
            val login = request.getParameter("login")
            val password = request.getParameter("password")

            if (login == null || password == null) {
                throw HttpProcessingException(400)
            }
            try {
                val loggedIn = playerInterface.login(login, password, remoteIp)
                if (loggedIn) {
                    loggedUserInterface.sendLogUserResponse(login, responseWriter)
                } else {
                    throw HttpProcessingException(403)
                }
            } catch (exp: PlayerBannedException) {
                throw HttpProcessingException(409)
            }
        }

    private fun executePasswordReset(): (uri: String, request: HttpRequest, remoteIp: String, responseWriter: ResponseWriter) -> Unit =
        { _, request, _, responseWriter ->
            val email = request.getParameter("email") ?: throw HttpProcessingException(400)
            playerInterface.resetPassword(email)
            responseWriter.writeXmlResponse(null)
        }

    private fun executePasswordResetValidate(): (
        uri: String,
        request: HttpRequest,
        remoteIp: String,
        responseWriter: ResponseWriter,
    ) -> Unit =
        { _, request, _, responseWriter ->
            val newPassword = request.getParameter("password")
            val resetToken = request.getParameter("resetToken")
            if (newPassword == null || resetToken == null) {
                throw HttpProcessingException(400)
            }
            playerInterface.resetPasswordValidate(newPassword, resetToken)
            responseWriter.writeXmlResponse(null)
        }

    private fun executeChangeEmail(): (
        uri: String,
        request: HttpRequest,
        remoteIp: String,
        responseWriter: ResponseWriter,
    ) -> Unit =
        { _, request, _, responseWriter ->
            val login = request.getParameter("login")
            val password = request.getParameter("password")
            val email = request.getParameter("email")
            if (login == null || password == null || email == null) {
                throw HttpProcessingException(400)
            }
            if (playerInterface.changeEmail(login, password, email)) {
                responseWriter.writeXmlResponse(null)
            } else {
                throw HttpProcessingException(403)
            }
        }

    private fun executeChangeEmailValidate(): (
        uri: String,
        request: HttpRequest,
        remoteIp: String,
        responseWriter: ResponseWriter,
    ) -> Unit =
        { _, request, _, responseWriter ->
            val changeEmailToken = request.getParameter("changeEmailToken") ?: throw HttpProcessingException(400)
            playerInterface.changeEmailValidate(changeEmailToken)
            responseWriter.writeXmlResponse(null)
        }

    override fun beforeContextStopped() {
        deregistration.forEach {
            it.run()
        }
        deregistration.clear()
    }
}
