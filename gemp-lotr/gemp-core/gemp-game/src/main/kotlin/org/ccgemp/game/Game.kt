package org.ccgemp.game

import com.gempukku.server.ResponseWriter

interface Game<ObserveSettings>: PlayedGame, FinishedGame {
    val gameSettings: GameSettings
    val gameParticipants: Array<GameParticipant>
    val gameResult: GameResult?

    fun processDecision(playerId: String, decisionId: String, decisionValue: String)

    fun setPlayerObserveSettings(playerId: String, settings: ObserveSettings)

    fun checkForTimeouts()

    fun joinGame(playerId: String, channelId: String, gameStream: GameStream<Any>)

    fun produceCardInfo(playerId: String, cardId: String, responseWriter: ResponseWriter): String

    fun leaveGame(channelId: String)

    fun finalizeGame()
}
