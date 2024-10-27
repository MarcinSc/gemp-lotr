package org.ccgemp.game

data class GameDeck(
    val name: String,
    val notes: String,
    val deckParts: Map<String, List<String>>,
)