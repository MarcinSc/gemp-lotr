package org.ccgemp.common

data class DefaultGempCollectionItem(
    override val product: String,
    override val count: Int,
) : GempCollectionItem
