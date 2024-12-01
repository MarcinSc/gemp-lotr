package org.ccgemp.tournament.renderer

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.ResponseWriter
import org.ccgemp.common.GameDeck
import org.ccgemp.tournament.TournamentClientInfo
import org.ccgemp.tournament.composite.standing.PlayerStanding
import org.hjson.JsonArray
import org.hjson.JsonObject
import java.time.format.DateTimeFormatter

@Exposes(TournamentModelRenderer::class)
class JsonTournamentModelRenderer : TournamentModelRenderer {
    private val minuteFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override fun renderGetTournamentDecks(player: String, decks: List<GameDeck>, responseWriter: ResponseWriter) {
        val root = JsonObject()
        root.set("player", player)
        val decksArray = JsonArray()
        decks.forEach { deck ->
            val deckObj = JsonObject()
            deckObj.set("name", deck.name)
            val parts = JsonArray()
            deck.deckParts.forEach { part ->
                val jsonPart = JsonObject()
                jsonPart.set("name", part.key)
                val cards = JsonObject()
                part.value.forEach { card ->
                    cards.set(card.card, card.count)
                }
                jsonPart.add("cards", cards)
                parts.add(jsonPart)
            }
            deckObj.set("contents", parts)
            decksArray.add(deckObj)
        }
        root.set("decks", decksArray)

        responseWriter.writeJsonResponse(root.toString())
    }

    override fun renderGetTournaments(tournaments: List<TournamentClientInfo>, responseWriter: ResponseWriter) {
        val root = JsonObject()
        val tournamentsArray = JsonArray()
        tournaments.forEach {
            val tournamentObj = JsonObject()
            tournamentObj.set("id", it.id)
            tournamentObj.set("name", it.name)
            tournamentObj.set("startDate", minuteFormatter.format(it.startDate))
            tournamentObj.set("status", it.status)
            tournamentsArray.add(tournamentObj)
        }
        root.add("tournaments", tournamentsArray)
        responseWriter.writeJsonResponse(root.toString())
    }

    override fun renderGetTournamentInfo(tournament: TournamentClientInfo, standings: List<PlayerStanding>, responseWriter: ResponseWriter) {
        val root = JsonObject()

        root.set("id", tournament.id)
        root.set("name", tournament.name)
        root.set("startDate", minuteFormatter.format(tournament.startDate))
        root.set("status", tournament.status)

        val standingsArray = JsonArray()
        standings.forEach { standing ->
            val standingObj = JsonObject()
            standingObj.set("name", standing.player)
            standingObj.set("standing", standing.standing)
            standingObj.set("points", standing.points)
            standing.stats.forEach {
                standingObj.set(it.key, it.value.toFloat())
            }
            standingsArray.add(standingObj)
        }
        root.set("standings", standingsArray)

        responseWriter.writeJsonResponse(root.toString())
    }

    override fun renderGetTournamentReport(tournament: TournamentClientInfo, standings: List<PlayerStanding>, responseWriter: ResponseWriter) {
        renderGetTournamentInfo(tournament, standings, responseWriter)
    }
}
