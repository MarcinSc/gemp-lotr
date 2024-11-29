package org.ccgemp.collection

import com.gempukku.context.resolver.expose.Exposes

@Exposes(ProductLibrary::class)
class TestProductLibrary : ProductLibrary {
    override fun getProductBox(name: String): ProductBox? {
        if (name == "selection") {
            return object : ProductBox {
                override fun openPack(): GempCollection {
                    return DefaultGempCollection().also {
                        it.addItem("product1", 1)
                        it.addItem("product2", 1)
                    }
                }
            }
        } else if (name == "pack") {
            return object : ProductBox {
                override fun openPack(): GempCollection {
                    return DefaultGempCollection().also {
                        it.addItem("product1", 1)
                        it.addItem("product2", 1)
                    }
                }
            }
        }
        return null
    }

    override fun isSelection(name: String): Boolean {
        return name == "selection"
    }
}
