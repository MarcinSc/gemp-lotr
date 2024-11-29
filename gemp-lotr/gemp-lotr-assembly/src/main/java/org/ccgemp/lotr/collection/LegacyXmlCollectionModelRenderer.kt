package org.ccgemp.lotr.collection

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.common.CardType
import com.gempukku.lotro.common.Side
import com.gempukku.lotro.game.LotroCardBlueprint
import com.gempukku.server.ResponseWriter
import org.ccgemp.collection.CollectionType
import org.ccgemp.collection.GempCollection
import org.ccgemp.collection.GempCollectionItem
import org.ccgemp.collection.ProductLibrary
import org.ccgemp.collection.renderer.CollectionModelRenderer
import org.ccgemp.lotr.LegacyObjectsProvider
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(CollectionModelRenderer::class)
class LegacyXmlCollectionModelRenderer : CollectionModelRenderer {
    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    @Inject
    private lateinit var productLibrary: ProductLibrary

    private val cardLibrary by lazy {
        legacyObjectsProvider.cardLibrary
    }

    override fun renderGetCollectionTypes(player: String, playerCollectionTypes: List<CollectionType>, responseWriter: ResponseWriter) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()

        val collectionsElem = doc.createElement("collections")

        playerCollectionTypes.forEach { collectionType ->
            val collectionElem = doc.createElement("collection")
            collectionElem.setAttribute("type", collectionType.type)
            collectionElem.setAttribute("name", collectionType.name)
            collectionElem.setAttribute("format", collectionType.format)
            collectionsElem.appendChild(collectionElem)
        }
        doc.appendChild(collectionsElem)

        responseWriter.writeXmlResponse(doc)
    }

    override fun renderOpenPack(player: String, packContents: GempCollection, responseWriter: ResponseWriter) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()

        doc.appendChild(serializeCollectionToXml(doc, packContents))

        responseWriter.writeXmlResponse(doc)
    }

    override fun renderGetCollection(
        player: String,
        filteredResult: List<GempCollectionItem>,
        start: Int,
        count: Int,
        responseWriter: ResponseWriter,
    ) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()

        doc.appendChild(serializeCardListToXml(doc, filteredResult, start, count))

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

    private fun serializeCardListToXml(
        document: Document,
        cardList: List<GempCollectionItem>,
        start: Int,
        count: Int,
    ): Element {
        val collectionElem = document.createElement("collection")
        collectionElem.setAttribute("count", cardList.size.toString())

        for (i in start until start + count) {
            if (i >= 0 && i < cardList.size) {
                val collectionItem = cardList[i]
                val item = collectionItem.product.toItem(collectionItem.count)
                val blueprintId = item.blueprintId
                if (item.type == com.gempukku.lotro.game.CardCollection.Item.Type.CARD) {
                    val card = document.createElement("card")
                    card.setAttribute("count", item.count.toString())
                    card.setAttribute("blueprintId", blueprintId)
                    val blueprint = cardLibrary.getLotroCardBlueprint(blueprintId)
                    appendCardSide(card, blueprint)
                    appendCardGroup(card, blueprint)
                    collectionElem.appendChild(card)
                } else {
                    val pack = document.createElement("pack")
                    pack.setAttribute("count", item.count.toString())
                    pack.setAttribute("blueprintId", blueprintId)
                    if (item.type == com.gempukku.lotro.game.CardCollection.Item.Type.SELECTION) {
                        val contents = productLibrary.findProductBox(blueprintId)!!.openPack()
                        val contentsStr = StringBuilder()
                        contents.all.forEach { item ->
                            contentsStr.append(item.product)
                        }
                        contentsStr.delete(contentsStr.length - 1, contentsStr.length)
                        pack.setAttribute("contents", contentsStr.toString())
                    }
                    collectionElem.appendChild(pack)
                }
            }
        }
        return collectionElem
    }

    private fun appendCardSide(card: Element, blueprint: LotroCardBlueprint) {
        val side = blueprint.side
        if (side != null) card.setAttribute("side", side.toString())
    }

    private fun appendCardGroup(card: Element, blueprint: LotroCardBlueprint) {
        val group =
            if (blueprint.cardType == CardType.THE_ONE_RING) {
                "ring"
            } else if (blueprint.cardType == CardType.SITE) {
                "site"
            } else if (blueprint.cardType == CardType.MAP) {
                "map"
            } else if (blueprint.canStartWithRing()) {
                "ringBearer"
            } else if (blueprint.side == Side.FREE_PEOPLE) {
                "fp"
            } else if (blueprint.side == Side.SHADOW) {
                "shadow"
            } else {
                null
            }
        if (group != null) card.setAttribute("group", group)
    }

    private fun String.toItem(count: Int): com.gempukku.lotro.game.CardCollection.Item {
        return com.gempukku.lotro.game.CardCollection.Item.createItem(this, count)
    }
}
