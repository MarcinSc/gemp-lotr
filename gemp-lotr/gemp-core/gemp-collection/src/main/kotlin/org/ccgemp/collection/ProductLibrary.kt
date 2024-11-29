package org.ccgemp.collection

interface ProductLibrary {
    fun findProductBox(name: String): ProductBox?

    fun isSelection(name: String): Boolean
}
