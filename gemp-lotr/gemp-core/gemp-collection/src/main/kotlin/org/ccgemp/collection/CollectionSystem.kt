package org.ccgemp.collection

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes

@Exposes(CollectionInterface::class)
class CollectionSystem : CollectionInterface {
    @Inject
    private lateinit var repository: CollectionRepository
    @Inject
    private lateinit var productLibrary: ProductLibrary

    private val collectionCache: MutableMap<CollectionCacheKey, CardCollection> = mutableMapOf()

    override fun findPlayerCollection(player: String, type: String): CardCollection? {
        val collectionCacheKey = CollectionCacheKey(player, type)
        return collectionCache.getOrPut(collectionCacheKey) {
            val collection = repository.findPlayerCollection(player, type) ?: return null
            val entries = repository.getPlayerCollectionEntries(setOf(collection))
            val result = DefaultCardCollection()
            entries.forEach {
                result.addItem(it.product!!, it.quantity)
            }
            result
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

    override fun getPlayerCollection(player: String, type: String): CardCollection? {
        val collectionCacheKey = CollectionCacheKey(player, type)
        return collectionCache.getOrPut(collectionCacheKey) {
            val collectionInfo = repository.findPlayerCollection(player, type) ?: return null
            val entries = repository.getPlayerCollectionEntries(setOf(collectionInfo)).groupBy { it.collection_id }[collectionInfo.id]
            val result = DefaultCardCollection()
            entries?.forEach {
                result.addItem(it.product!!, it.quantity)
            }
            result
        }
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

    override fun openPackInCollection(player: String, type: String, packId: String, selection: String?): CardCollection? {
        val collectionInfo = repository.findPlayerCollection(player, type) ?: return null
        val count = repository.getItemCount(player, type, packId)
        if (count < 1) {
            return null
        }

        val removeCollection = DefaultCardCollection()
        removeCollection.addItem(packId, 1)

        val productBox = productLibrary.getProductBox(packId) ?: return null
        val openedPack = productBox.openPack()
        if (productLibrary.isSelection(packId)) {
            if (selection == null || selection !in openedPack) {
                return null
            }

            val addCollection = DefaultCardCollection()
            addCollection.addItem(selection, 1)

            repository.removeFromCollection(collectionInfo, CollectionChange(false, "Opened pack", removeCollection))
            repository.addToCollection(collectionInfo, CollectionChange(true, "Opened pack", addCollection))

            collectionCache.remove(CollectionCacheKey(player, type))

            return addCollection
        } else {
            val addCollection = DefaultCardCollection()
            openedPack.forEach {
                addCollection.addItem(it, 1)
            }

            repository.removeFromCollection(collectionInfo, CollectionChange(false, "Opened pack", removeCollection))
            repository.addToCollection(collectionInfo, CollectionChange(true, "Opened pack", addCollection))

            collectionCache.remove(CollectionCacheKey(player, type))

            return addCollection
        }
    }
}

private data class CollectionCacheKey(val player: String, val type: String)
