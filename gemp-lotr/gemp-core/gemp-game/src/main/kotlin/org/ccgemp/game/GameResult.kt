package org.ccgemp.game

import java.time.LocalDateTime

data class GameResult(
    val finishTime: LocalDateTime,
    val cancelled: Boolean,
    val winner: String?,
)
