package org.ccgemp.deck

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.AuthorizedApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.ServerRequestHandler
import org.ccgemp.deck.renderer.DeckModelRenderer

class DeckApiSystem : AuthorizedApiSystem() {
    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var deckModelRenderer: DeckModelRenderer

    @Inject
    private lateinit var deckDeserializer: DeckDeserializer

    @InjectValue("server.deck.urlPrefix")
    private lateinit var urlPrefix: String

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
            val actingAsUser = getActingAsUser(request)

            val playerDecks = deckInterface.getPlayerDecks(actingAsUser.userId)

            deckModelRenderer.renderListDecks(actingAsUser.userId, playerDecks, responseWriter)
        }

    private fun executeGetDeck(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(request)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            val deck = deckInterface.findDeck(actingAsUser.userId, deckName) ?: throw HttpProcessingException(404)

            deckModelRenderer.renderGetDeck(actingAsUser.userId, deck, responseWriter)
        }

    private fun executeSaveDeck(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(request)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)
            val targetFormat = request.getParameter("targetFormat") ?: throw HttpProcessingException(400)
            val notes = request.getParameter("notes") ?: throw HttpProcessingException(400)
            val deckContents = request.getParameter("deckContents") ?: throw HttpProcessingException(400)

            val deck = deckDeserializer.deserializeDeck(deckName, targetFormat, notes, deckContents) ?: throw HttpProcessingException(400)

            deckInterface.saveDeck(actingAsUser.userId, deck)

            responseWriter.writeXmlResponse(null)
        }

    private fun executeRenameDeck(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val actingAsUser = getActingAsUser(request)
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
            val actingAsUser = getActingAsUser(request)
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            deckInterface.deleteDeck(actingAsUser.userId, deckName)

            responseWriter.writeXmlResponse(null)
        }
}
