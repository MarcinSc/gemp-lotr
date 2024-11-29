package org.ccgemp.collection

interface GempCollection {
    val all: Collection<GempCollectionItem>

    fun getItemCount(identifier: String): Int
}
