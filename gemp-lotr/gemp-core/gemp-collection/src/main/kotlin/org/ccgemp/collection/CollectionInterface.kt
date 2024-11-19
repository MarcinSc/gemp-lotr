package org.ccgemp.collection

interface CollectionInterface {
    fun findPlayerCollection(player: String, type: String): CardCollection?

    fun addPlayerCollection(player: String, type: String, collectionChange: CollectionChange): Boolean

    fun addToPlayerCollection(player: String, type: String, collectionChange: CollectionChange): Boolean

    fun getPlayerCollections(type: String): Map<String, CardCollection>
}
