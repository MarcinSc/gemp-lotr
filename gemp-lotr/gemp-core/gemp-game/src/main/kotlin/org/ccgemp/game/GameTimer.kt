package org.ccgemp.game

data class GameTimer(
    val name: String,
    val maxSecondsPerPlayer: Int,
    val maxSecondsPerDecision: Int,
) {
    override fun toString(): String {
        return "This game table uses the '$name' timer. Each player has a total time bank of " +
                "${(maxSecondsPerPlayer / 60)} minutes, and will time out with a loss if they take longer than " +
                "${(maxSecondsPerDecision / 60)} minutes between actions."
    }
}
