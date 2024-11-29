package org.ccgemp.transfer.renderer

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.ResponseWriter
import org.ccgemp.collection.GempCollection
import org.hjson.JsonArray
import org.hjson.JsonObject

@Exposes(TransferModelRenderer::class)
class JsonTransferModelRenderer : TransferModelRenderer {
    override fun renderGetDelivery(player: String, transfers: Map<String, GempCollection>, responseWriter: ResponseWriter) {
        val root = JsonObject()
        root.set("player", player)
        val transfersJson = JsonObject()
        transfers.forEach { (type, collection) ->
            val collectionJson = JsonArray()
            collection.all.forEach { collectionItem ->
                val obj = JsonObject()
                obj.set("product", collectionItem.product)
                obj.set("count", collectionItem.count)
                collectionJson.add(obj)
            }
            transfersJson.add(type, collectionJson)
        }
        root.set("transfers", transfersJson)

        responseWriter.writeJsonResponse(root.toString())
    }
}
