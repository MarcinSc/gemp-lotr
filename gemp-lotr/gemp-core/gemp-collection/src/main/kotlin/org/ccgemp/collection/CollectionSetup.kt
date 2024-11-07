package org.ccgemp.collection

fun createCollectionSystems(): List<Any> {
    return listOf(
        FilterAndSortSystem(),
        CardFilterAndSortSystem(),
    )
}