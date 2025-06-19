package org.ccgemp.collection

data class DefaultGempCollectionItem(
    override val product: String,
    override val count: Int,
) : GempCollectionItem
