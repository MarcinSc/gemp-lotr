package org.ccgemp.tournament

interface TournamentProgress {
    fun updateState(round: Int, stage: String)

    fun createMatch(recipe: TournamentGameRecipe)

    fun awardBye(round: Int, player: String)

    fun dropPlayer(player: String)
}
