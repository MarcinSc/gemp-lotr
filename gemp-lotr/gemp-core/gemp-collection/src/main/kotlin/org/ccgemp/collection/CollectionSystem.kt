package org.ccgemp.collection

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes

@Exposes(CollectionInterface::class)
class CollectionSystem : CollectionInterface {
    @Inject
    private lateinit var repository: CollectionRepository

    private val collectionCache: MutableMap<CollectionCacheKey, CardCollection> = mutableMapOf()

    override fun findPlayerCollection(player: String, type: String): CardCollection? {
        val collectionCacheKey = CollectionCacheKey(player, type)
        return collectionCache[collectionCacheKey] ?: run {
            val collection = repository.findPlayerCollection(player, type) ?: return null
            val entries = repository.getPlayerCollectionEntries(setOf(collection))
            val result = DefaultCardCollection()
            entries.forEach {
                result.addItem(it.product!!, it.quantity)
            }
            result.also {
                collectionCache[collectionCacheKey] = result
            }
        }
    }

    override fun addPlayerCollection(player: String, type: String, collectionChange: CollectionChange): Boolean {
        val collectionInfo = repository.findPlayerCollection(player, type)
        if (collectionInfo != null) {
            return false
        }

        val newCollectionInfo = repository.createCollection(player, type)
        repository.addToCollection(newCollectionInfo, collectionChange)

        collectionCache.remove(CollectionCacheKey(player, type))

        return true
    }

    override fun addToPlayerCollection(player: String, type: String, collectionChange: CollectionChange): Boolean {
        val collectionInfo = repository.findPlayerCollection(player, type) ?: return false

        repository.addToCollection(collectionInfo, collectionChange)

        collectionCache.remove(CollectionCacheKey(player, type))

        return true
    }

    override fun getPlayerCollections(type: String): Map<String, CardCollection> {
        val collectionInfos = repository.findCollectionsByType(type)
        val entries = repository.getPlayerCollectionEntries(collectionInfos.toSet()).groupBy { it.collection_id }
        return collectionInfos.associate { collection ->
            collection.player!! to
                run {
                    val result = DefaultCardCollection()
                    entries[collection.id]?.forEach {
                        result.addItem(it.product!!, it.quantity)
                    }
                    result
                }
        }.onEach { (player, collection) ->
            collectionCache[CollectionCacheKey(player, type)] = collection
        }
    }
}

private data class CollectionCacheKey(val player: String, val type: String)
