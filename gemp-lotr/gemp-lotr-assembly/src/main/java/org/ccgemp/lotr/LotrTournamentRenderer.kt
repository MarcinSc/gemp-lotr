package org.ccgemp.lotr

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.common.Names
import com.gempukku.lotro.game.CardCollection
import com.gempukku.lotro.game.CardItem
import com.gempukku.lotro.game.DefaultCardCollection
import com.gempukku.lotro.game.LotroCardBlueprint
import com.gempukku.lotro.logic.GameUtils
import org.apache.commons.text.StringEscapeUtils.escapeHtml3
import org.ccgemp.collection.FilterAndSort
import org.ccgemp.deck.GameDeck
import org.ccgemp.tournament.TournamentClientInfo
import org.ccgemp.tournament.TournamentRenderer

@Exposes(TournamentRenderer::class)
class LotrTournamentRenderer : TournamentRenderer {
    @Inject
    private lateinit var objectsProvider: LegacyObjectsProvider

    @Inject
    private lateinit var filterAndSort: FilterAndSort<CardItem>

    override fun renderDecksHtml(tournament: TournamentClientInfo, player: String): String {
        return surroundWithReadoutHeaderAndFooter(renderDecks(tournament.players.first { it.player == player }.decks.values, player))
    }

    override fun renderReportHtml(tournament: TournamentClientInfo): String {
        TODO("Not yet implemented")
    }

    override fun renderInfoHtml(tournament: TournamentClientInfo): String {
        TODO("Not yet implemented")
    }

    private fun surroundWithReadoutHeaderAndFooter(fragments: List<String>): String {
        val preamble =
            """
            <html>
                <style>
                    body {
                        margin:50;
                    }
                    
                    .tooltip {
                      border-bottom: 1px dotted black; /* If you want dots under the hoverable text */
                      color:#0000FF;
                    }
                    
                    .tooltip span, .tooltip title {
                        display:none;
                    }
                    .tooltip:hover span:not(.click-disabled),.tooltip:active span:not(.click-disabled) {
                        display:block;
                        position:fixed;
                        overflow:hidden;
                        background-color: #FAEBD7;
                        width:auto;
                        z-index:9999;
                        top:20%;
                        left:350px;
                    }
                    /* This prevents tooltip images from automatically shrinking if they are near the window edge.*/
                    .tooltip span > img {
                        max-width:none !important;
                        overflow:hidden;
                    }
                                    
                </style>
                <body>
            """.trimIndent()
        val divider = "<br/>[hr]<hr><br/>"
        val postamble = "</body></html>"
        return preamble + fragments.joinToString(divider) + postamble
    }

    private fun renderDecks(decks: Collection<GameDeck>, player: String): List<String> {
        return decks.map { renderDeck(it, player) }
    }

    private fun renderDeck(deck: GameDeck, player: String?): String {
        val blueprintLibrary = objectsProvider.cardLibrary

        val lotroDeck = deck.toLotroDeck()

        val result = StringBuilder()
        result.append("<div>")
            .append("<h1>").append(escapeHtml3(lotroDeck.deckName)).append("</h1>")
            .append("<h2>Format: ").append(escapeHtml3(lotroDeck.targetFormat)).append("</h2>")

        if (player != null) {
            result.append("<h2>Author: ").append(escapeHtml3(player)).append("</h2>")
        }

        val ringBearer = lotroDeck.ringBearer
        if (ringBearer != null) {
            result.append("<b>Ring-bearer:</b> ").append(generateCardTooltip(ringBearer)).append("<br/>")
        }
        val ring = lotroDeck.ring
        if (ring != null) {
            result.append("<b>Ring:</b> ").append(generateCardTooltip(ring)).append("<br/>")
        }

        val deckCards = DefaultCardCollection()
        for (card in lotroDeck.drawDeckCards) {
            deckCards.addItem(blueprintLibrary.getBaseBlueprintId(card), 1)
        }
        for (site in lotroDeck.sites) {
            deckCards.addItem(blueprintLibrary.getBaseBlueprintId(site), 1)
        }

        result.append("<br/>")
        result.append("<b>Adventure deck:</b><br/>")
        for (item in filterAndSort.process(
            "cardType:SITE",
            "siteNumber,twilight",
            deckCards.all,
        )) {
            result.append(generateCardTooltip(item)).append("<br/>")
        }

        result.append("<br/>")
        result.append("<b>Free Peoples Draw Deck:</b><br/>")
        for (item in filterAndSort.process(
            "side:FREE_PEOPLE",
            "cardType,culture,name",
            deckCards.all,
        )) {
            result.append(item.count).append("x ").append(generateCardTooltip(item)).append("<br/>")
        }

        result.append("<br/>")
        result.append("<b>Shadow Draw Deck:</b><br/>")
        for (item in filterAndSort.process(
            "side:SHADOW",
            "cardType,culture,name",
            deckCards.all,
        )) {
            result.append(item.count).append("x ").append(generateCardTooltip(item)).append("<br/>")
        }

        result.append("<h3>Notes</h3><br>").append(deck.notes.replace("\n", "<br/>"))

        result.append("</div>")

        return result.toString()
    }

    private fun generateCardTooltip(item: CardCollection.Item): String {
        return generateCardTooltip(objectsProvider.cardLibrary.getLotroCardBlueprint(item.blueprintId), item.blueprintId)
    }

    private fun generateCardTooltip(bpid: String): String {
        return generateCardTooltip(objectsProvider.cardLibrary.getLotroCardBlueprint(bpid), bpid)
    }

    private fun generateCardTooltip(bp: LotroCardBlueprint, bpid: String): String {
        val parts = bpid.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var setnum = parts[0].toInt()
        var set = String.format("%02d", setnum)
        var subset = "S"
        var version = 0
        if (setnum >= 50 && setnum <= 69) {
            setnum -= 50
            set = String.format("%02d", setnum)
            subset = "E"
            version = 1
        } else if (setnum >= 70 && setnum <= 89) {
            setnum -= 70
            set = String.format("%02d", setnum)
            subset = "E"
            version = 1
        } else if (setnum >= 100 && setnum <= 149) {
            setnum -= 100
            set = "V$setnum"
        }
        val cardnum = parts[1].replace("*", "").replace("T", "").toInt()

        val id = "LOTR-EN" + set + subset + String.format("%03d", cardnum) + "." + String.format("%01d", version)
        var displayName = Names.SanitizeDisplayName(GameUtils.getFullName(bp))
        if (subset == "E") {
            displayName += " (Errata)"
        }
        val result = (
                "<span class=\"tooltip\">" + displayName +
                        "<span><img class=\"ttimage\" src=\"https://wiki.lotrtcgpc.net/images/" + id + "_card.jpg\" ></span></span>"
                )

        return result
    }
}
