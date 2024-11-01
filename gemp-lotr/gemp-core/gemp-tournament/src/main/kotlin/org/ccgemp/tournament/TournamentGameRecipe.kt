package org.ccgemp.tournament

import org.ccgemp.game.GameSettings

data class TournamentGameRecipe(
    val round: Int,
    val participants: Array<String>,
    val gameSettings: GameSettings,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TournamentGameRecipe

        if (!participants.contentEquals(other.participants)) return false
        if (gameSettings != other.gameSettings) return false

        return true
    }

    override fun hashCode(): Int {
        var result = participants.contentHashCode()
        result = 31 * result + gameSettings.hashCode()
        return result
    }
}
