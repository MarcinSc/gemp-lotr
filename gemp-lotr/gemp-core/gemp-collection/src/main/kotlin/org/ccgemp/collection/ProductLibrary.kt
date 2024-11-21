package org.ccgemp.collection

interface ProductLibrary {
    fun getProductBox(name: String): ProductBox?
    fun isSelection(name: String): Boolean
}