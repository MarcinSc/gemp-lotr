package org.ccgemp.deck

interface DeckRepository {
    fun findDeck(player: String, name: String): GameDeckInfo?
}