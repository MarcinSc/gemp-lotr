package org.ccgemp.common

interface GempCollection {
    val all: Iterable<GempCollectionItem>

    fun getItemCount(identifier: String): Int
}
