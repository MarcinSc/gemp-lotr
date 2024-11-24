package org.ccgemp.game

interface FinishedGame {
    val gameId: String
    val formatName: String
    val info: String
    val status: String
    val players: List<String>
    val private: Boolean
    val winner: String?
}
