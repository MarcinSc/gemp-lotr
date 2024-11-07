package org.ccgemp.collection

import java.util.function.Predicate

interface CardFilterAndSortRegistry<CollectionItem> {
    val defaultSort: Comparator<CollectionItem>

    fun createPredicate(type: String, value: String): Predicate<CollectionItem>

    fun createSort(value: List<String>): Comparator<CollectionItem>

    fun registerCardFilter(type: String, predicateProvider: (String.() -> Predicate<CollectionItem>))

    fun registerSorter(type: String, comparator: Comparator<CollectionItem>)
}
