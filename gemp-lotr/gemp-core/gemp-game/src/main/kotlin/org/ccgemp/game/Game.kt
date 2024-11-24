package org.ccgemp.game

interface Game: PlayedGame, FinishedGame {
    val gameSettings: GameSettings
    val gameParticipants: Array<GameParticipant>
    val gameResult: GameResult?

    fun processDecision(playerId: String, decisionId: String, decisionValue: String)

    fun checkForTimeouts()

    fun joinGame(playerId: String, channelId: String, gameStream: GameStream<Any>)

    fun leaveGame(channelId: String)

    fun finalizeGame()
}
