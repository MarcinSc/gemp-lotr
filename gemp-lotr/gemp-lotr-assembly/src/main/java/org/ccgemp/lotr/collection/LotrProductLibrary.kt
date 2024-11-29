package org.ccgemp.lotr.collection

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.collection.DefaultGempCollection
import org.ccgemp.collection.GempCollection
import org.ccgemp.collection.ProductBox
import org.ccgemp.collection.ProductLibrary
import org.ccgemp.lotr.LegacyObjectsProvider

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
            override fun openPack(): GempCollection {
                val result = DefaultGempCollection()
                productBox.openPack().forEach {
                    result.addItem(it.blueprintId, it.count)
                }
                return result
            }
        }
    }

    override fun isSelection(name: String): Boolean {
        return name.startsWith("(S)")
    }
}
