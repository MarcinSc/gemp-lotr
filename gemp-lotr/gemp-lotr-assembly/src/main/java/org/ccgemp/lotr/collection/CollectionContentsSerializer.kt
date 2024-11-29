package org.ccgemp.lotr.collection

import org.ccgemp.collection.GempCollection
import org.ccgemp.collection.GempCollectionItem
import org.w3c.dom.Document
import org.w3c.dom.Element

interface CollectionContentsSerializer {
    fun serializeCollectionToXml(document: Document, collection: GempCollection): Element

    fun serializeCardListToXml(
        document: Document,
        cardList: List<GempCollectionItem>,
        start: Int,
        count: Int,
    ): Element
}