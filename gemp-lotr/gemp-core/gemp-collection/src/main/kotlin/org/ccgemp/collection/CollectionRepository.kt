package org.ccgemp.collection

interface CollectionRepository {
    fun findPlayerCollection(player: String, type: String): CollectionInfo?

    fun getPlayerCollectionEntries(collection: Set<CollectionInfo>): List<CollectionEntryInfo>

    fun createCollection(player: String, type: String): CollectionInfo

    fun addToCollection(collectionInfo: CollectionInfo, collection: CardCollection)

    fun findCollectionsByType(type: String): List<CollectionInfo>
}
