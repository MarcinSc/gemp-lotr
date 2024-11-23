package org.ccgemp.collection

interface CollectionTypeProvider {
    fun getCollectionTypes(player: String): List<CollectionType>
}
