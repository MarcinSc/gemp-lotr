package org.ccgemp.collection

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes

@Exposes(FilterAndSort::class)
class FilterAndSortSystem : FilterAndSort {
    @Inject
    private lateinit var cardFilterAndSortProvider: CardFilterAndSortRegistry

    override fun process(filter: String, cards: Iterable<String>): List<String> {
        val filterParams = filter.split(" ").map {
            val parts = it.split(delimiters = arrayOf(":"), limit = 2)
            parts[0] to parts[1]
        }
        val nonSortFilters = filterParams.filterNot { it.first.equals("sort", true) }
        val sort = filterParams.firstOrNull { it.first.equals("sort", true) }


        val comparator = sort?.let {
            cardFilterAndSortProvider.createSort(sort.second.split(","))
        } ?: cardFilterAndSortProvider.defaultSort

        val predicates = nonSortFilters.map { cardFilterAndSortProvider.createPredicate(it.first, it.second) }
        val filterResult = cards.filter { card -> predicates.all { it.test(card) } }

        return filterResult.sortedWith(comparator)
    }
}