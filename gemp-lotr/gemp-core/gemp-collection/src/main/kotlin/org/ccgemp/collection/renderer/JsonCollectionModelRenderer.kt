package org.ccgemp.collection.renderer

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.ResponseWriter
import org.ccgemp.collection.CollectionType
import org.ccgemp.collection.GempCollection
import org.ccgemp.collection.GempCollectionItem
import org.hjson.JsonArray
import org.hjson.JsonObject

@Exposes(CollectionModelRenderer::class)
class JsonCollectionModelRenderer : CollectionModelRenderer {
    override fun renderGetCollection(
        player: String,
        collectionItems: List<GempCollectionItem>,
        start: Int,
        count: Int,
        responseWriter: ResponseWriter,
    ) {
        val root = JsonObject()
        root.set("player", player)
        root.set("totalCount", collectionItems.size)
        root.set("start", start)
        root.set("count", count)
        val items = JsonArray()
        collectionItems.stream().skip(start.toLong()).toList().take(count).forEach { collectionItem ->
            val obj = JsonObject()
            obj.set("product", collectionItem.product)
            obj.set("count", collectionItem.count)
            items.add(obj)
        }
        root.set("items", items)

        responseWriter.writeJsonResponse(root.toString())
    }

    override fun renderGetCollectionTypes(player: String, playerCollectionTypes: List<CollectionType>, responseWriter: ResponseWriter) {
        val root = JsonObject()
        root.set("player", player)
        val items = JsonArray()
        playerCollectionTypes.forEach { collectionType ->
            val obj = JsonObject()
            obj.set("type", collectionType.type)
            obj.set("name", collectionType.name)
            obj.set("format", collectionType.format)
            items.add(obj)
        }
        root.set("items", items)

        responseWriter.writeJsonResponse(root.toString())
    }

    override fun renderOpenPack(player: String, packContents: GempCollection, responseWriter: ResponseWriter) {
        val root = JsonObject()
        root.set("player", player)
        val items = JsonArray()
        packContents.all.forEach { item ->
            val obj = JsonObject()
            obj.set("product", item.product)
            obj.set("count", item.count)
            items.add(obj)
        }
        root.set("items", items)

        responseWriter.writeJsonResponse(root.toString())
    }
}
