package org.ccgemp.game

data class GameSettings(
    val format: String,
    val private: Boolean,
    val timeSettings: GameTimer,
    val description: String,
    val decisionLeniencySeconds: Int,
)
