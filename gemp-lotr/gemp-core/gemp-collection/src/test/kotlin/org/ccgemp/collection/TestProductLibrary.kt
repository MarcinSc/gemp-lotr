package org.ccgemp.collection

import com.gempukku.context.resolver.expose.Exposes

@Exposes(ProductLibrary::class)
class TestProductLibrary : ProductLibrary {
    override fun getProductBox(name: String): ProductBox? {
        if (name == "selection") {
            return object : ProductBox {
                override fun openPack(): List<String> {
                    return listOf("product1", "product2")
                }
            }
        } else if (name == "pack") {
            return object : ProductBox {
                override fun openPack(): List<String> {
                    return listOf("product1", "product2")
                }
            }
        }
        return null
    }

    override fun isSelection(name: String): Boolean {
        return name == "selection"
    }
}
