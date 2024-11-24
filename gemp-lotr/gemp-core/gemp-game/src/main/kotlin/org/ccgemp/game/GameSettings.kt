package org.ccgemp.game

data class GameSettings(
    val format: String,
    val private: Boolean,
    val watchable: Boolean,
    val timeSettings: GameTimer,
    val info: String,
    val decisionLeniencySeconds: Int,
)
