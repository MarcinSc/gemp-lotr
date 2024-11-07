package org.ccgemp.collection

interface FilterAndSort<CardCollection> {
    fun <T : CardCollection> process(filter: String, sort: String?, cards: Iterable<T>): List<T>
}
