package org.ccgemp.collection

import org.ccgemp.common.CardCollectionItem

data class DefaultCardCollectionItem(
    override val product: String,
    override val count: Int,
) : CardCollectionItem
