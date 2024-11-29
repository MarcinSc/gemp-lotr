package org.ccgemp.collection

interface GempCollection {
    val all: Iterable<GempCollectionItem>

    fun getItemCount(identifier: String): Int
}
