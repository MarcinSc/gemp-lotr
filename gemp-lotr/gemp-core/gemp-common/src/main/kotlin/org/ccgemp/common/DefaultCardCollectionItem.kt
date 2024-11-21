package org.ccgemp.common

data class DefaultCardCollectionItem(
    override val product: String,
    override val count: Int,
) : CardCollectionItem
