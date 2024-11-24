package org.ccgemp.game

interface GameProducer<ObserveSettings> {
    fun createGame(gameId: String, gameParticipants: Array<GameParticipant>, gameSettings: GameSettings): Game<ObserveSettings>
}
