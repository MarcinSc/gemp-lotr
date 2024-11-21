package org.ccgemp.common

interface CardCollection {
    val all: Iterable<CardCollectionItem>

    fun getItemCount(identifier: String): Int
}
