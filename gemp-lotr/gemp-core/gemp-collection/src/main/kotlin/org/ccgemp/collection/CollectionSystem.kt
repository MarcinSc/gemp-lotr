package org.ccgemp.collection

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectList
import com.gempukku.context.resolver.expose.Exposes

@Exposes(CollectionInterface::class)
class CollectionSystem : CollectionInterface {
    @Inject
    private lateinit var repository: CollectionRepository

    @Inject
    private lateinit var productLibrary: ProductLibrary

    @InjectList
    private lateinit var collectionTypeProviders: List<CollectionTypeProvider>

    @InjectList
    private lateinit var transferObservers: List<TransferObserver>

    private val collectionCache: MutableMap<CollectionCacheKey, GempCollection> = mutableMapOf()

    override fun getPlayerCollectionTypes(player: String): List<CollectionType> {
        return collectionTypeProviders.flatMap { it.getCollectionTypes(player) }
    }

    override fun findPlayerCollection(player: String, type: String): GempCollection? {
        val collectionCacheKey = CollectionCacheKey(player, type)
        return collectionCache.getOrPut(collectionCacheKey) {
            val collection = repository.findPlayerCollection(player, type) ?: return null
            val entries = repository.getPlayerCollectionEntries(setOf(collection))
            val result = DefaultGempCollection()
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
        internalAddToCollection(newCollectionInfo, collectionChange)

        collectionCache.remove(CollectionCacheKey(player, type))

        return true
    }

    override fun addToPlayerCollection(player: String, type: String, collectionChange: CollectionChange): Boolean {
        val collectionInfo = repository.findPlayerCollection(player, type) ?: return false

        internalAddToCollection(collectionInfo, collectionChange)

        collectionCache.remove(CollectionCacheKey(player, type))

        return true
    }

    override fun getPlayerCollections(type: String): Map<String, GempCollection> {
        val collectionInfos = repository.findCollectionsByType(type)
        val entries = repository.getPlayerCollectionEntries(collectionInfos.toSet()).groupBy { it.collection_id }
        return collectionInfos.associate { collection ->
            collection.player!! to
                run {
                    val result = DefaultGempCollection()
                    entries[collection.id]?.forEach {
                        result.addItem(it.product!!, it.quantity)
                    }
                    result
                }
        }.onEach { (player, collection) ->
            collectionCache[CollectionCacheKey(player, type)] = collection
        }
    }

    override fun openPackInCollection(
        player: String,
        type: String,
        packId: String,
        selection: String?,
    ): GempCollection? {
        val collectionInfo = repository.findPlayerCollection(player, type) ?: return null
        val count = repository.getItemCount(player, type, packId)
        if (count < 1) {
            return null
        }

        val removeCollection = DefaultGempCollection()
        removeCollection.addItem(packId, 1)

        val productBox = productLibrary.findProductBox(packId) ?: return null
        val openedPack = productBox.openPack()
        if (productLibrary.isSelection(packId)) {
            if (selection == null || openedPack.getItemCount(selection) < 1) {
                return null
            }

            val addCollection = DefaultGempCollection()
            addCollection.addItem(selection, openedPack.getItemCount(selection))

            internalRemoveFromCollection(collectionInfo, CollectionChange(false, "Opened pack", removeCollection))
            internalAddToCollection(collectionInfo, CollectionChange(true, "Opened pack", addCollection))

            collectionCache.remove(CollectionCacheKey(player, type))

            return addCollection
        } else {
            internalRemoveFromCollection(collectionInfo, CollectionChange(false, "Opened pack", removeCollection))
            internalAddToCollection(collectionInfo, CollectionChange(true, "Opened pack", openedPack))

            collectionCache.remove(CollectionCacheKey(player, type))

            return openedPack
        }
    }

    private fun internalRemoveFromCollection(collectionInfo: CollectionInfo, collectionChange: CollectionChange) {
        repository.removeFromCollection(collectionInfo, collectionChange)
        transferObservers.forEach { it.transferredFrom(collectionInfo.player!!, collectionChange.reason, collectionInfo.type!!, collectionChange.collection) }
    }

    private fun internalAddToCollection(collectionInfo: CollectionInfo, collectionChange: CollectionChange) {
        repository.addToCollection(collectionInfo, collectionChange)
        transferObservers.forEach {
            it.transferredTo(
                collectionInfo.player!!,
                collectionChange.reason,
                collectionChange.notify,
                collectionInfo.type!!,
                collectionChange.collection,
            )
        }
    }
}

private data class CollectionCacheKey(val player: String, val type: String)