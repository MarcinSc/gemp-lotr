package org.ccgemp.lotr.deck

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.common.Side
import com.gempukku.lotro.game.CardCollection
import com.gempukku.lotro.game.DefaultCardCollection
import com.gempukku.lotro.game.LotroCardBlueprint
import com.gempukku.lotro.game.LotroFormat
import com.gempukku.lotro.logic.GameUtils
import com.gempukku.lotro.logic.vo.LotroDeck
import com.gempukku.server.HttpProcessingException
import org.apache.commons.lang3.StringEscapeUtils
import org.ccgemp.collection.FilterAndSort
import org.ccgemp.common.GameDeck
import org.ccgemp.format.GempFormats
import org.ccgemp.lotr.LegacyObjectsProvider

@Exposes(HtmlDeckSerializer::class)
class LotrHtmlDeckSerializer : HtmlDeckSerializer {
    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    @Inject
    private lateinit var filterAndSort: FilterAndSort<Any>

    @Inject
    private lateinit var gempFormats: GempFormats<LotroFormat>

    override fun serializeDeck(author: String?, deck: GameDeck): String {
        val lotroDeck = deck.toLotroDeck()
        val cardLibrary = legacyObjectsProvider.cardLibrary
        val formatLibrary = legacyObjectsProvider.formatLibrary

        val result = StringBuilder()
        result.append(
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
            """.trimIndent(),
        )
        result.append("<h1>" + StringEscapeUtils.escapeHtml3(deck.name) + "</h1>")
        result.append("<h2>Format: " + StringEscapeUtils.escapeHtml3(deck.targetFormat) + "</h2>")
        if (author != null) {
            result.append("<h2>Author: " + StringEscapeUtils.escapeHtml3(author) + "</h2>")
        }
        val ringBearer: String = lotroDeck.getRingBearer()
        if (ringBearer != null) result.append("<b>Ring-bearer:</b> " + generateCardTooltip(cardLibrary.getLotroCardBlueprint(ringBearer), ringBearer) + "<br/>")
        val ring: String = lotroDeck.getRing()
        if (ring != null) result.append("<b>Ring:</b> " + generateCardTooltip(cardLibrary.getLotroCardBlueprint(ring), ring) + "<br/>")

        val format: LotroFormat = formatLibrary.getFormatByName(deck.targetFormat)
        if (format != null && format.usesMaps()) {
            val map: String = lotroDeck.getMap()
            if (map != null) result.append("<b>Map:</b> " + generateCardTooltip(cardLibrary.getLotroCardBlueprint(map), map) + "<br/>")
        }

        val deckCards = DefaultCardCollection()
        for (card in lotroDeck.drawDeckCards) deckCards.addItem(cardLibrary.getBaseBlueprintId(card), 1)
        for (site in lotroDeck.sites) deckCards.addItem(cardLibrary.getBaseBlueprintId(site), 1)

        result.append("<br/>")
        result.append("<b>Adventure deck:</b><br/>")
        for (item in filterAndSort.process("cardType:SITE", "siteNumber,twilight", deckCards.all)) result.append(
            generateCardTooltip(item.blueprintId) + "<br/>",
        )

        result.append("<br/>")
        result.append("<b>Free Peoples Draw Deck:</b><br/>")
        for (item in filterAndSort.process<CardCollection.Item>(
            "side:FREE_PEOPLE",
            "cardType,culture,name",
            deckCards.all,
        )) result.append(item.count.toString() + "x " + generateCardTooltip(item.blueprintId) + "<br/>")

        result.append("<br/>")
        result.append("<b>Shadow Draw Deck:</b><br/>")
        for (item in filterAndSort.process<CardCollection.Item>(
            "side:SHADOW",
            "cardType,culture,name",
            deckCards.all,
        )) result.append(item.count.toString() + "x " + generateCardTooltip(item.blueprintId) + "<br/>")

        result.append("<h3>Notes</h3><br>" + deck.notes.replace("\n", "<br/>"))

        result.append("</body></html>")

        return result.toString()
    }

    override fun serializeValidation(deck: GameDeck, targetFormat: String): String {
        val lotroDeck = deck.toLotroDeck()

        var fpCount = 0
        var shadowCount = 0
        for (card in lotroDeck.getDrawDeckCards()) {
            val side: Side = legacyObjectsProvider.cardLibrary.getLotroCardBlueprint(card).getSide()
            if (side == Side.SHADOW) {
                shadowCount++
            } else if (side == Side.FREE_PEOPLE) {
                fpCount++
            }
        }

        val sb = java.lang.StringBuilder()
        sb.append("<b>Free People</b>: $fpCount, <b>Shadow</b>: $shadowCount<br/>")

        val valid = java.lang.StringBuilder()
        val invalid = java.lang.StringBuilder()

        val format = validateFormat(targetFormat) ?: throw HttpProcessingException(400)

        val validation = format.validateDeck(lotroDeck)
        var errataValidation: List<String>? = null
        if (format.errataCardMap.isNotEmpty()) {
            val deckWithErrata: LotroDeck = format.applyErrata(lotroDeck)
            errataValidation = format.validateDeck(deckWithErrata)
        }
        if (validation.size == 0) {
            valid.append("<b>" + format.name + "</b>: <font color='green'>Valid</font><br/>")
        } else if (errataValidation != null && errataValidation.isEmpty()) {
            valid.append("<b>" + format.name + "</b>: <font color='green'>Valid</font> <font color='yellow'>(with errata automatically applied)</font><br/>")
            val output = java.lang.String.join("<br>", validation).replace("\n", "<br>")
            invalid.append("<font color='yellow'>$output</font><br/>")
        } else {
            val output = java.lang.String.join("<br>", validation).replace("\n", "<br>")
            invalid.append("<b>" + format.name + "</b>: <font color='red'>" + output + "</font><br/>")
        }

        sb.append(valid)
        sb.append(invalid)

        return sb.toString()
    }

    private fun validateFormat(name: String): LotroFormat? {
        var validatedFormat = legacyObjectsProvider.formatLibrary.getFormat(name)
        if (validatedFormat == null) {
            validatedFormat =
                try {
                    legacyObjectsProvider.formatLibrary.getFormatByName(name)
                } catch (ex: Exception) {
                    legacyObjectsProvider.formatLibrary.getFormatByName("Anything Goes")
                }
        }

        return validatedFormat
    }

    private fun generateCardTooltip(item: String): String {
        return generateCardTooltip(legacyObjectsProvider.cardLibrary.getLotroCardBlueprint(item), item)
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
        val result = (
            "<span class=\"tooltip\">" + GameUtils.getFullName(bp) +
                "<span><img class=\"ttimage\" src=\"https://wiki.lotrtcgpc.net/images/" + id + "_card.jpg\" ></span></span>"
        )

        return result
    }
}
