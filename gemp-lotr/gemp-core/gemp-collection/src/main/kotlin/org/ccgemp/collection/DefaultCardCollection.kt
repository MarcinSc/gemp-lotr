package org.ccgemp.collection

import org.ccgemp.common.CardCollection
import org.ccgemp.common.CardCollectionItem

class DefaultCardCollection : CardCollection {
    override val all: Iterable<CardCollectionItem>
        get() = items.values

    private val items: MutableMap<String, CardCollectionItem> = mutableMapOf()

    constructor()

    constructor(collection: CardCollection) {
        collection.all.forEach {
            addItem(it.product, it.count)
        }
    }

    override fun getItemCount(identifier: String): Int {
        return items[identifier]?.count ?: 0
    }

    fun addItem(product: String, quantity: Int) {
        items.compute(product) { _, oldValue ->
            DefaultCardCollectionItem(product, (oldValue?.count ?: 0) + quantity)
        }
    }

    fun removeItem(product: String, quantity: Int): Boolean {
        val item = items[product]
        if (item == null || item.count < quantity) {
            return false
        }
        if (item.count == quantity) {
            items.remove(product)
        } else {
            items.compute(product) { _, oldValue ->
                DefaultCardCollectionItem(product, oldValue!!.count - quantity)
            }
        }
        return true
    }
}
