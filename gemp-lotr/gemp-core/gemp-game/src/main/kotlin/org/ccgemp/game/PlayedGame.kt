package org.ccgemp.game

interface PlayedGame {
    val gameId: String
    val formatName: String
    val info: String
    var status: String
    val players: List<String>
    val private: Boolean
    val watchable: Boolean
}
