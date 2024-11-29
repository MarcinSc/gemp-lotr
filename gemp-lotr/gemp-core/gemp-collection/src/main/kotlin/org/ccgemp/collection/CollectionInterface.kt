package org.ccgemp.collection

import org.ccgemp.common.GempCollection

interface CollectionInterface {
    fun getPlayerCollectionTypes(player: String): List<CollectionType>

    fun findPlayerCollection(player: String, type: String): GempCollection?

    fun addPlayerCollection(player: String, type: String, collectionChange: CollectionChange): Boolean

    fun addToPlayerCollection(player: String, type: String, collectionChange: CollectionChange): Boolean

    fun getPlayerCollections(type: String): Map<String, GempCollection>

    fun openPackInCollection(
        player: String,
        type: String,
        packId: String,
        selection: String?,
    ): GempCollection?
}
