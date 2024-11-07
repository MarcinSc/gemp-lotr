package org.ccgemp.collection

interface FilterAndSort {
    fun process(filter: String, cards: Iterable<String>): List<String>
}