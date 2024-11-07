package org.ccgemp.collection

import com.gempukku.context.processor.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import java.util.function.Predicate

@Exposes(CardFilterAndSortRegistry::class)
class CardFilterAndSortSystem : CardFilterAndSortRegistry {
    @InjectValue("collection.filter.defaultSort")
    private lateinit var defaultSortStr: String

    private val filterRegistry: MutableMap<String, (String.() -> Predicate<String>)> = mutableMapOf()
    private val sorterRegistry: MutableMap<String, Comparator<String>> = mutableMapOf()

    override val defaultSort: Comparator<String> by lazy {
        createSort(defaultSortStr.split(","))
    }

    override fun createPredicate(type: String, value: String): Predicate<String> {
        return filterRegistry[type.lowercase()]!!.invoke(value)
    }

    override fun createSort(value: List<String>): Comparator<String> {
        val comparators = value.map { sorterRegistry[it.lowercase()]!! }
        return comparators.reduce { first, second ->
            first.thenComparing(second)
        }
    }

    override fun registerCardFilter(type: String, predicateProvider: String.() -> Predicate<String>) {
        filterRegistry[type.lowercase()] = predicateProvider
    }

    override fun registerSorter(type: String, comparator: Comparator<String>) {
        sorterRegistry[type.lowercase()] = comparator
    }
}