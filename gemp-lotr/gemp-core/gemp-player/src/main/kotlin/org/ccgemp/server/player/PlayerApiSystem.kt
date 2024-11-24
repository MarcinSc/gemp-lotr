package org.ccgemp.server.player

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.ApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServer
import com.gempukku.server.ResponseWriter
import com.gempukku.server.login.LoggedUserInterface

class PlayerApiSystem : ApiSystem() {
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

    override fun registerAPIs(): List<Registration> {
        return listOf(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$loginUrl$",
                executeLogin(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$registerUrl$",
                executeRegister(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$passwordResetUrl$",
                executePasswordReset(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$passwordResetValidateUrl$",
                executePasswordResetValidate(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$changeEmailUrl$",
                executeChangeEmail(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$changeEmailValidateUrl$",
                executeChangeEmailValidate(),
            ),
        )
    }

    private fun executeRegister(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val login = request.getParameter("login")
            val password = request.getParameter("password")
            val email = request.getParameter("email")

            if (login == null || password == null || email == null) {
                throw HttpProcessingException(400)
            }
            try {
                val registered = playerInterface.register(login, password, email, request.remoteIp)
                if (registered) {
                    loggedUserInterface.sendLogUserResponse(login, responseWriter)
                } else {
                    throw HttpProcessingException(409)
                }
            } catch (exp: LoginInvalidException) {
                throw HttpProcessingException(400)
            }
        }

    private fun executeLogin(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val login = request.getParameter("login")
            val password = request.getParameter("password")

            if (login == null || password == null) {
                throw HttpProcessingException(400)
            }
            try {
                val loggedIn = playerInterface.login(login, password, request.remoteIp)
                if (loggedIn) {
                    loggedUserInterface.sendLogUserResponse(login, responseWriter)
                } else {
                    throw HttpProcessingException(403)
                }
            } catch (exp: PlayerBannedException) {
                throw HttpProcessingException(409)
            }
        }

    private fun executePasswordReset(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val email = request.getParameter("email") ?: throw HttpProcessingException(400)
            playerInterface.resetPassword(email)
            responseWriter.writeXmlResponse(null)
        }

    private fun executePasswordResetValidate(): (
        request: HttpRequest,
        responseWriter: ResponseWriter,
    ) -> Unit =
        { request, responseWriter ->
            val newPassword = request.getParameter("password")
            val resetToken = request.getParameter("resetToken")
            if (newPassword == null || resetToken == null) {
                throw HttpProcessingException(400)
            }
            playerInterface.resetPasswordValidate(newPassword, resetToken)
            responseWriter.writeXmlResponse(null)
        }

    private fun executeChangeEmail(): (
        request: HttpRequest,
        responseWriter: ResponseWriter,
    ) -> Unit =
        { request, responseWriter ->
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
        request: HttpRequest,
        responseWriter: ResponseWriter,
    ) -> Unit =
        { request, responseWriter ->
            val changeEmailToken = request.getParameter("changeEmailToken") ?: throw HttpProcessingException(400)
            playerInterface.changeEmailValidate(changeEmailToken)
            responseWriter.writeXmlResponse(null)
        }
}
