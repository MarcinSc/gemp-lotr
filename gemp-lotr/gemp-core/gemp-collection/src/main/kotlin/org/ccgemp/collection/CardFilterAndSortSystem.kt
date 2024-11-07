package org.ccgemp.collection

import com.gempukku.context.processor.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.common.splitText
import java.util.function.Predicate

@Exposes(CardFilterAndSortRegistry::class)
class CardFilterAndSortSystem<CollectionItem> : CardFilterAndSortRegistry<CollectionItem> {
    @InjectValue("collection.filter.defaultSort")
    private lateinit var defaultSortStr: String

    private val filterRegistry: MutableMap<String, (String.() -> Predicate<CollectionItem>)> = mutableMapOf()
    private val sorterRegistry: MutableMap<String, Comparator<CollectionItem>> = mutableMapOf()

    override val defaultSort: Comparator<CollectionItem> by lazy {
        createSort(defaultSortStr.splitText(','))
    }

    override fun createPredicate(type: String, value: String): Predicate<CollectionItem> {
        return filterRegistry[type.lowercase()]!!.invoke(value)
    }

    override fun createSort(value: List<String>): Comparator<CollectionItem> {
        val comparators = value.map { sorterRegistry[it.lowercase()]!! }
        return comparators.reduce { first, second ->
            first.thenComparing(second)
        }
    }

    override fun registerCardFilter(type: String, predicateProvider: String.() -> Predicate<CollectionItem>) {
        filterRegistry[type.lowercase()] = predicateProvider
    }

    override fun registerSorter(type: String, comparator: Comparator<CollectionItem>) {
        sorterRegistry[type.lowercase()] = comparator
    }
}
