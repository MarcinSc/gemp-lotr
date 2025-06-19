package org.ccgemp.collection.renderer

import com.gempukku.server.ResponseWriter
import org.ccgemp.collection.CollectionType
import org.ccgemp.collection.GempCollection
import org.ccgemp.collection.GempCollectionItem

interface CollectionModelRenderer {
    fun renderGetCollectionTypes(player: String, playerCollectionTypes: List<CollectionType>, responseWriter: ResponseWriter)

    fun renderOpenPack(player: String, packContents: GempCollection, responseWriter: ResponseWriter)

    fun renderGetCollection(
        player: String,
        filteredResult: List<GempCollectionItem>,
        start: Int,
        count: Int,
        responseWriter: ResponseWriter,
    )
}
