package org.ccgemp.collection

data class DefaultCardCollectionItem(
    override val product: String,
    override val count: Int,
) : CardCollectionItem
