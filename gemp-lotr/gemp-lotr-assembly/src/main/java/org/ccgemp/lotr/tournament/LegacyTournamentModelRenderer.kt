package org.ccgemp.lotr.tournament

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.common.Names
import com.gempukku.lotro.game.CardCollection
import com.gempukku.lotro.game.CardItem
import com.gempukku.lotro.game.DefaultCardCollection
import com.gempukku.lotro.game.LotroCardBlueprint
import com.gempukku.lotro.logic.GameUtils
import com.gempukku.server.ResponseWriter
import org.apache.commons.text.StringEscapeUtils.escapeHtml3
import org.ccgemp.collection.FilterAndSort
import org.ccgemp.common.GameDeck
import org.ccgemp.lotr.LegacyObjectsProvider
import org.ccgemp.lotr.deck.toLotroDeck
import org.ccgemp.tournament.TournamentClientInfo
import org.ccgemp.tournament.composite.standing.PlayerStanding
import org.ccgemp.tournament.renderer.TournamentModelRenderer
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(TournamentModelRenderer::class)
class LegacyTournamentModelRenderer : TournamentModelRenderer {
    @Inject
    private lateinit var objectsProvider: LegacyObjectsProvider

    @Inject
    private lateinit var filterAndSort: FilterAndSort<CardItem>

    private val minuteFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override fun renderGetTournaments(tournaments: List<TournamentClientInfo>, responseWriter: ResponseWriter) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()
        val tournamentsElem = doc.createElement("tournaments")

        tournaments.forEach { tournament ->
            val tournamentElem = doc.createElement("tournament")
            tournamentElem.setAttribute("id", tournament.id)
            tournamentElem.setAttribute("name", tournament.name)
            tournamentElem.setAttribute("startDate", minuteFormatter.format(tournament.startDate))
            tournamentElem.setAttribute("stage", tournament.status)
            tournamentsElem.appendChild(tournamentElem)
        }

        doc.appendChild(tournamentsElem)

        responseWriter.writeXmlResponse(doc)
    }

    override fun renderGetTournamentDecks(player: String, decks: List<GameDeck>, responseWriter: ResponseWriter) {
        responseWriter.writeHtmlResponse(
            surroundWithReadoutHeaderAndFooter(renderDecks(decks, player)),
        )
    }

    override fun renderGetTournamentInfo(tournament: TournamentClientInfo, standings: List<PlayerStanding>, responseWriter: ResponseWriter) {
        responseWriter.writeXmlResponse(
            renderInfo(tournament, standings),
        )
    }

    override fun renderGetTournamentReport(tournament: TournamentClientInfo, standings: List<PlayerStanding>, responseWriter: ResponseWriter) {
        responseWriter.writeHtmlResponse(
            renderReportHtml(tournament, standings),
        )
    }

    private fun renderReportHtml(tournament: TournamentClientInfo, standings: List<PlayerStanding>): String {
        val tournamentStart = tournament.startDate

        val games = tournament.matches

        val lastRound = games.maxOf { it.round }

        val summary = java.lang.StringBuilder()
        summary
            .append("<h1>").append(escapeHtml3(tournament.name)).append("</h1>")
            .append("<ul>")
            .append("<li>Total Rounds: ").append(lastRound).append("</li>")
            .append("<li>Start: ").append(tournamentStart.format(minuteFormatter)).append("</li>")
            .append("</ul><br/><br/><hr>")

        val sections = ArrayList<String>()
        sections.add(summary.toString())

        for (standing in standings) {
            val playerName = standing.name

            val player = tournament.players.firstOrNull { it.player == playerName }
            if (player != null) {
                sections.add("<h2>"+player.player+"</h2>")
                player.decks.values.forEach { deck ->
                    sections.add(renderDeck(deck, null))
                }
            }
        }

        return surroundWithReadoutHeaderAndFooter(sections)
    }

    private fun renderInfo(tournament: TournamentClientInfo, standings: List<PlayerStanding>): Document {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()

        val tournamentElem: Element = doc.createElement("tournament")

        tournamentElem.setAttribute("id", tournament.id)
        tournamentElem.setAttribute("name", tournament.name)
        tournamentElem.setAttribute("round", tournament.round.toString())
        tournamentElem.setAttribute("stage", tournament.status)

        for (standing in standings) {
            val standingElem: Element = doc.createElement("tournamentStanding")
            setStandingAttributes(standing, standingElem)
            tournamentElem.appendChild(standingElem)
        }

        doc.appendChild(tournamentElem)

        return doc
    }

    private fun setStandingAttributes(standing: PlayerStanding, standingElem: Element) {
        standingElem.setAttribute("player", standing.name)
        standingElem.setAttribute("standing", standing.standing.toString())
        standingElem.setAttribute("points", standing.points.toString())
        standingElem.setAttribute("gamesPlayed", standing.stats["gamesPlayer"].toString())
        val format = DecimalFormat("##0.00%")
        standingElem.setAttribute("opponentWin", format.format(standing.stats["opponentWinRate"]?.toDouble()))
        standingElem.setAttribute("medianScore", standing.stats["medianScore"].toString())
        standingElem.setAttribute("cumulativeScore", standing.stats["cumulativeScore"].toString())
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
