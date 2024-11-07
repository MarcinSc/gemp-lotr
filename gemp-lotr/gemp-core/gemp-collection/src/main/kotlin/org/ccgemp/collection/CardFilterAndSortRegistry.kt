package org.ccgemp.collection

import java.util.function.Predicate

interface CardFilterAndSortRegistry {
    val defaultSort: Comparator<String>

    fun createPredicate(type: String, value: String): Predicate<String>
    fun createSort(value: List<String>): Comparator<String>

    fun registerCardFilter(type: String, predicateProvider: (String.() -> Predicate<String>))
    fun registerSorter(type: String, comparator: Comparator<String>)
}