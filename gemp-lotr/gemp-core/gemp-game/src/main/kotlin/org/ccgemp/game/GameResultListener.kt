package org.ccgemp.game

interface GameResultListener {
    fun gameCancelled(gameId: String)

    fun gameFinished(gameId: String, participants: Array<GameParticipant>, winner: String)
}
