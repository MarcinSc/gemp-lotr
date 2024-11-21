package org.ccgemp.collection

import org.ccgemp.common.CardCollection
import org.ccgemp.common.CardCollectionItem
import org.w3c.dom.Document

interface CollectionContentsSerializer {
    fun serializePackToXml(packCollection: CardCollection): Document

    fun serializeCardListToXml(cardList: List<CardCollectionItem>, start: Int, count: Int): Document
}
