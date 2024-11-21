package org.ccgemp.collection

import org.w3c.dom.Document

interface CollectionContentsSerializer {
    fun serializePackToXml(pack: CardCollection): Document

    fun serializeCardListToXml(cardList: List<CardCollectionItem>, start: Int, count: Int): Document
}
