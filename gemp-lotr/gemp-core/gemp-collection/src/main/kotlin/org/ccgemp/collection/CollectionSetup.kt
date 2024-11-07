package org.ccgemp.collection

fun createCollectionSystems(): List<Any> {
    return listOf(
        CollectionSystem(),
        DbCollectionRepository(),
        FilterAndSortSystem<Any>(),
        CardFilterAndSortSystem<Any>(),
    )
}
