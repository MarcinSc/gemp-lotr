package org.ccgemp.game

interface Game {
    val gameSettings: GameSettings
    val gameFinished: Long?
    val gameParticipants: Array<GameParticipant>

    fun processDecision(playerId: String, decisionId: String, decisionValue: String)

    fun checkForTimeouts()

    fun joinGame(playerId: String, channelId: String, gameStream: GameStream<Any>)

    fun leaveGame(channelId: String)

    fun finalizeGame()
}
