package org.ccgemp.lotr

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.collection.ProductBox
import org.ccgemp.collection.ProductLibrary

@Exposes(ProductLibrary::class)
class LotrProductLibrary : ProductLibrary {
    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    private val productLibrary by lazy {
        legacyObjectsProvider.productLibrary
    }

    override fun getProductBox(name: String): ProductBox? {
        val productBox = productLibrary.GetProduct(name) ?: return null
        return object : ProductBox {
            override fun openPack(): List<String> {
                return productBox.openPack().flatMap {
                    val product = it.blueprintId
                    val count = it.count
                    generateSequence { product }.take(count)
                }
            }
        }
    }

    override fun isSelection(name: String): Boolean {
        return name.startsWith("(S)")
    }
}
