package org.ccgemp.collection

fun createCollectionSystems(): List<Any> {
    return listOf(
        CollectionSystem(),
        BaseDbCollectionRepository(),
        FilterAndSortSystem<Any>(),
        CardFilterAndSortSystem<Any>(),
    )
}
