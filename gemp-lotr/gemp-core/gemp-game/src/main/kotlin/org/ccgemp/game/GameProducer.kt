package org.ccgemp.game

import java.util.function.Consumer

interface GameProducer {
    fun createGame(gameId: String, gameParticipants: Array<GameParticipant>, gameSettings: GameSettings, statusConsumer: Consumer<String>): Game
}
