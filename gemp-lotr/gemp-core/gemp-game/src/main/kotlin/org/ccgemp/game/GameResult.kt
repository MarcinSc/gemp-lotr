package org.ccgemp.game

data class GameResult(
    val finishTime: Long,
    val cancelled: Boolean,
    val winner: String?,
)
