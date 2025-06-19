package org.ccgemp.lotr.deck

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.ApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.ServerRequestHandler
import org.ccgemp.common.GameDeck
import org.ccgemp.deck.DeckInterface
import org.ccgemp.deck.renderer.DeckModelRenderer
import org.ccgemp.lotr.LegacyObjectsProvider

class LegacyLibrarianDeckApiSystem : ApiSystem() {
    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var deckModelRenderer: DeckModelRenderer

    @Inject
    private lateinit var htmlDeckSerializer: HtmlDeckSerializer

    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    @InjectValue("server.deck.urlPrefix")
    private lateinit var urlPrefix: String

    @InjectValue("librarian.name")
    private lateinit var librarianName: String

    private val defaultSortComparator =
        Comparator.comparing { deck: GameDeck ->
            val format = legacyObjectsProvider.formatLibrary.getFormat(deck.targetFormat)
            String.format("%02d", format.order) + format.name + deck.name
        }

    override fun registerAPIs(): List<Registration> {
        return listOf(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/libraryList$",
                executeLibraryList(),
            ),
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/library$",
                executeGetLibraryDeck(),
            ),
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/libraryHtml$",
                executeGetLibraryDeckHtml(),
            ),
        )
    }

    private fun executeLibraryList(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val librarianDecks = deckInterface.getPlayerDecks(librarianName)

            responseWriter.writeXmlResponse(
                (deckModelRenderer as LegacyDeckModelRenderer).renderDeckList(
                    librarianDecks,
                    Comparator.comparing<GameDeck?, Boolean?> {
                        it.name.contains("Starter")
                    }.thenComparing(defaultSortComparator),
                ),
            )
        }

    private fun executeGetLibraryDeck(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            val deck = deckInterface.findDeck(librarianName, deckName) ?: throw HttpProcessingException(404)

            deckModelRenderer.renderGetDeck(librarianName, deck, responseWriter)
        }

    private fun executeGetLibraryDeckHtml(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            val deckName = request.getParameter("deckName") ?: throw HttpProcessingException(400)

            val deck = deckInterface.findDeck(librarianName, deckName) ?: throw HttpProcessingException(404)

            responseWriter.writeHtmlResponse(htmlDeckSerializer.serializeDeck(null, deck))
        }
}
