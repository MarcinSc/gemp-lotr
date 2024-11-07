package org.ccgemp.tournament.composite

import org.ccgemp.game.GameSettings

interface MatchesTournamentProcess : TournamentProcess {
    val rounds: Int
    val deckType: String
    val gameSettings: GameSettings
    val pairingGroup: String
    val byeGroup: String
}
