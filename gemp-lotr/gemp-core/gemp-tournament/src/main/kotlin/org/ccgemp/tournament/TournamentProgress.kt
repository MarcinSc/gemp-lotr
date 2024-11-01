package org.ccgemp.tournament

interface TournamentProgress {
    fun updateStage(stage: String)

    fun setRound(round: Int)

    fun createMatch(recipe: TournamentGameRecipe)

    fun awardBye(round: Int, player: String)
}
