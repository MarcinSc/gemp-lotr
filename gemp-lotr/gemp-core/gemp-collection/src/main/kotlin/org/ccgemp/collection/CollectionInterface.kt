package org.ccgemp.collection

interface CollectionInterface {
    fun findPlayerCollection(player: String, type: String): CardCollection?

    fun addPlayerCollection(
        player: String,
        type: String,
        notify: Boolean,
        reason: String,
        collection: CardCollection,
    ): Boolean

    fun addToPlayerCollection(
        player: String,
        type: String,
        notify: Boolean,
        reason: String,
        collection: CardCollection,
    ): Boolean

    fun getPlayerCollections(type: String): Map<String, CardCollection>
}
