package org.ccgemp.game

interface GameProducer {
    fun createGame(
        gameParticipants: Array<GameParticipant>,
        gameSettings: GameSettings,
    ): Game
}
