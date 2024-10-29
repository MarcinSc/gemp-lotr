package org.ccgemp.game

interface GameContainerInterface<Event> {
    fun createNewGame(
        participants: Array<GameParticipant>,
        gameSettings: GameSettings,
    ): String

    fun joinGame(
        gameId: String,
        playerId: String,
        admin: Boolean,
        gameStream: GameStream<Event>,
    ): Runnable?

    fun processPlayerDecision(
        gameId: String,
        playerId: String,
        decisionId: String,
        decisionValue: String,
    ): Boolean
}
