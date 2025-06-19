package org.ccgemp.collection

import org.ccgemp.collection.renderer.CollectionModelRenderer
import org.ccgemp.collection.renderer.JsonCollectionModelRenderer

fun createCollectionSystems(
    collectionRepository: CollectionRepository = BaseDbCollectionRepository(),
    collectionModelRenderer: CollectionModelRenderer = JsonCollectionModelRenderer(),
    productLibrary: ProductLibrary,
): List<Any> {
    return listOf(
        CollectionSystem(),
        collectionRepository,
        FilterAndSortSystem<Any>(),
        CardFilterAndSortSystem<Any>(),
        CollectionApiSystem(),
        collectionModelRenderer,
        productLibrary,
    )
}
