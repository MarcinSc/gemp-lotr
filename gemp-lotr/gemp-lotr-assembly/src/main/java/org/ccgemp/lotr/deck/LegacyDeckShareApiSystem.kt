package org.ccgemp.lotr.deck

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.AuthorizedApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.ServerRequestHandler
import org.ccgemp.deck.DeckInterface
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class LegacyDeckShareApiSystem : AuthorizedApiSystem() {
    @Inject
    private lateinit var deckInterface: DeckInterface

    @Inject
    private lateinit var htmlDeckSerializer: HtmlDeckSerializer

    @InjectValue("server.deck.urlPrefix")
    private lateinit var urlPrefix: String

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
            val actingAsUser = getActingAsUser(request)
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
