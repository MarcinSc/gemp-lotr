package org.ccgemp.collection

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.common.splitText

@Exposes(FilterAndSort::class)
class FilterAndSortSystem<CollectionItem> : FilterAndSort<CollectionItem> {
    @Inject
    private lateinit var cardFilterAndSortProvider: CardFilterAndSortRegistry<CollectionItem>

    override fun <T : CollectionItem> process(filter: String, sort: String?, cards: Iterable<T>): List<T> {
        val filterParams =
            filter.splitText(' ').map {
                val parts = it.splitText(':', 2)
                parts[0] to parts[1]
            }

        val comparator =
            sort?.let {
                cardFilterAndSortProvider.createSort(sort.splitText(','))
            } ?: cardFilterAndSortProvider.defaultSort

        val predicates = filterParams.map { cardFilterAndSortProvider.createPredicate(it.first, it.second) }
        val filterResult = cards.filter { card -> predicates.all { it.test(card) } }

        return filterResult.sortedWith(comparator)
    }
}
