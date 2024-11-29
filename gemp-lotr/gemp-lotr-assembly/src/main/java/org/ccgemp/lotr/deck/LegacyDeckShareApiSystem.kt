package org.ccgemp.lotr.deck

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.ApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.ServerRequestHandler
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.UserRolesProvider
import com.gempukku.server.login.getActingAsUser
import org.ccgemp.deck.DeckDeserializer
import org.ccgemp.deck.DeckInterface
import org.ccgemp.lotr.LegacyObjectsProvider
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class LegacyDeckShareApiSystem : ApiSystem() {
    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var deckDeserializer: DeckDeserializer

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    @Inject
    private lateinit var htmlDeckSerializer: HtmlDeckSerializer

    @InjectValue("server.deck.urlPrefix")
    private lateinit var urlPrefix: String

    @InjectValue("roles.admin")
    private lateinit var adminRole: String

    @InjectValue("parameterNames.actAsParameter")
    private lateinit var actAsParameter: String

    override fun registerAPIs(): List<Registration> {
        return listOf(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/share",
                executeShareDeck(),
            ),
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/getShared",
                executeGetShared(),
            ),
        )
    }

    private fun executeShareDeck(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            val url = "$urlPrefix/getShared?id="

            val code: String = actingAsUser.userId + "|" + deckName

            val base64 = Base64.getEncoder().encodeToString(code.toByteArray(StandardCharsets.UTF_8))
            val result = URLEncoder.encode(base64, StandardCharsets.UTF_8)

            responseWriter.writeHtmlResponse(url + result)
        }

    private fun executeGetShared(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val shareCode = request.getParameter("id") ?: throw HttpProcessingException(400)
            val code = String(Base64.getDecoder().decode(shareCode), StandardCharsets.UTF_8)
            val fields = code.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (fields.size != 2) throw HttpProcessingException(400)

            val deck = deckInterface.findDeck(fields[0], fields[1]) ?: throw HttpProcessingException(404)

            responseWriter.writeHtmlResponse(htmlDeckSerializer.serializeDeck(fields[0], deck))
        }
}
