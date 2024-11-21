package org.ccgemp.lotr

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.common.CardType
import com.gempukku.lotro.common.Side
import com.gempukku.lotro.game.LotroCardBlueprint
import org.ccgemp.common.CollectionContentsSerializer
import org.ccgemp.common.GempCollection
import org.ccgemp.common.GempCollectionItem
import org.w3c.dom.Document
import org.w3c.dom.Element

@Exposes(CollectionContentsSerializer::class)
class LotrCollectionContentsSerializer : CollectionContentsSerializer {
    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    private val cardLibrary by lazy {
        legacyObjectsProvider.cardLibrary
    }
    private val productLibrary by lazy {
        legacyObjectsProvider.productLibrary
    }

    override fun serializeCollectionToXml(document: Document, collection: GempCollection): Element {
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

    override fun serializeCardListToXml(document: Document, cardList: List<GempCollectionItem>, start: Int, count: Int): Element {
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
                        val contents = productLibrary.GetProduct(blueprintId).openPack()
                        val contentsStr = StringBuilder()
                        for (content in contents) contentsStr.append(content.blueprintId).append("|")
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
