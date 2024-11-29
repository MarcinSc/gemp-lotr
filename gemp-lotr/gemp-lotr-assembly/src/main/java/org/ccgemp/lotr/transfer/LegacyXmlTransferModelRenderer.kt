package org.ccgemp.lotr.transfer

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.game.LotroCardBlueprint
import com.gempukku.server.ResponseWriter
import org.ccgemp.collection.GempCollection
import org.ccgemp.lotr.LegacyObjectsProvider
import org.ccgemp.transfer.renderer.TransferModelRenderer
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(TransferModelRenderer::class)
class LegacyXmlTransferModelRenderer : TransferModelRenderer {
    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    private val cardLibrary by lazy {
        legacyObjectsProvider.cardLibrary
    }

    override fun renderGetDelivery(player: String, transfers: Map<String, GempCollection>, responseWriter: ResponseWriter) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()

        val deliveryElem = doc.createElement("delivery")

        transfers.forEach {
            val collection = serializeCollectionToXml(doc, it.value)
            collection.setAttribute("type", it.key)
            deliveryElem.appendChild(collection)
        }

        doc.appendChild(deliveryElem)

        responseWriter.writeXmlResponse(doc)
    }

    private fun serializeCollectionToXml(document: Document, collection: GempCollection): Element {
        val collectionElem = document.createElement("collection")

        for (collectionItem in collection.all) {
            val product = collectionItem.product
            val item = product.toItem(collectionItem.count)
            if (item.type == com.gempukku.lotro.game.CardCollection.Item.Type.CARD) {
                val card = document.createElement("card")
                card.setAttribute("count", item.count.toString())
                card.setAttribute("blueprintId", item.blueprintId)
                appendCardSide(card, cardLibrary.getLotroCardBlueprint(item.blueprintId))
                collectionElem.appendChild(card)
            } else {
                val pack = document.createElement("pack")
                pack.setAttribute("count", item.count.toString())
                pack.setAttribute("blueprintId", item.blueprintId)
                collectionElem.appendChild(pack)
            }
        }
        return collectionElem
    }

    private fun appendCardSide(card: Element, blueprint: LotroCardBlueprint) {
        val side = blueprint.side
        if (side != null) card.setAttribute("side", side.toString())
    }

    private fun String.toItem(count: Int): com.gempukku.lotro.game.CardCollection.Item {
        return com.gempukku.lotro.game.CardCollection.Item.createItem(this, count)
    }
}
