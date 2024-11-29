package org.ccgemp.deck

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

class DeckApiSystem : ApiSystem() {
    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var deckSerializer: DeckSerializer

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
                "^$urlPrefix/list$",
                executeListDecks(),
            ),
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix$",
                executeGetDeck(),
            ),
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
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix$",
                executeSaveDeck(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/rename$",
                executeRenameDeck(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/delete$",
                executeDeleteDeck(),
            ),
        )
    }

    private fun executeListDecks(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)

            val playerDecks = deckInterface.getPlayerDecks(actingAsUser.userId)

            responseWriter.writeXmlResponse(deckSerializer.renderDeckList(playerDecks))
        }

    private fun executeGetDeck(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            val deck = deckInterface.findDeck(actingAsUser.userId, deckName) ?: throw HttpProcessingException(404)

            responseWriter.writeXmlResponse(deckSerializer.renderDeck(deck))
        }

    private fun executeGetDeckHtml(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            val deck = deckInterface.findDeck(actingAsUser.userId, deckName) ?: throw HttpProcessingException(404)

            responseWriter.writeHtmlResponse(htmlDeckSerializer.serializeDeck(deck, actingAsUser.userId))
        }

    private fun executeSaveDeck(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)
            val targetFormat = request.getParameter("targetFormat") ?: throw HttpProcessingException(400)
            val notes = request.getParameter("notes") ?: throw HttpProcessingException(400)
            val deckContents = request.getParameter("deckContents") ?: throw HttpProcessingException(400)

            val deck = deckSerializer.deserializeDeck(deckName, targetFormat, notes, deckContents) ?: throw HttpProcessingException(400)

            deckInterface.saveDeck(actingAsUser.userId, deck)

            responseWriter.writeXmlResponse(null)
        }

    private fun executeRenameDeck(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)
            val oldDeckName = request.getParameter("oldDeckName") ?: throw HttpProcessingException(400)

            val result = deckInterface.renameDeck(actingAsUser.userId, oldDeckName, deckName)
            if (!result) {
                throw HttpProcessingException(404)
            }

            responseWriter.writeXmlResponse(null)
        }

    private fun executeDeleteDeck(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            deckInterface.deleteDeck(actingAsUser.userId, deckName)

            responseWriter.writeXmlResponse(null)
        }

    private fun executeGetDeckStats(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val targetFormat = request.getParameter("targetFormat") ?: throw HttpProcessingException(400)
            val deckContents = request.getParameter("deckContents") ?: throw HttpProcessingException(400)

            val deck = deckSerializer.deserializeDeck("Temp Deck", targetFormat, "", deckContents) ?: throw HttpProcessingException(400)

            responseWriter.writeHtmlResponse(htmlDeckSerializer.serializeValidation(deck, targetFormat))
        }
}
