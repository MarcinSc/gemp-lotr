package org.ccgemp.collection

interface CardCollection {
    val all: Iterable<CardCollectionItem>

    fun getItemCount(identifier: String): Int
}
