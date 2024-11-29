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

class LegacyHtmlDeckApiSystem : ApiSystem() {
    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var deckDeserializer: DeckDeserializer

    @Inject
    private lateinit var htmlDeckSerializer: HtmlDeckSerializer

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

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
                "^$urlPrefix/html$",
                executeGetDeckHtml(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/stats",
                executeGetDeckStats(),
            ),
        )
    }

    private fun executeGetDeckHtml(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            val deck = deckInterface.findDeck(actingAsUser.userId, deckName) ?: throw HttpProcessingException(404)

            responseWriter.writeHtmlResponse(htmlDeckSerializer.serializeDeck(actingAsUser.userId, deck))
        }

    private fun executeGetDeckStats(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val targetFormat = request.getParameter("targetFormat") ?: throw HttpProcessingException(400)
            val deckContents = request.getParameter("deckContents") ?: throw HttpProcessingException(400)

            val deck = deckDeserializer.deserializeDeck("Temp Deck", targetFormat, "", deckContents) ?: throw HttpProcessingException(400)

            responseWriter.writeHtmlResponse(htmlDeckSerializer.serializeValidation(deck, targetFormat))
        }
}
