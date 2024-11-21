package org.ccgemp.transfer

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.common.CardCollection
import org.ccgemp.common.DefaultCardCollection
import org.ccgemp.common.mergeTexts
import org.ccgemp.common.splitText

@Exposes(TransferInterface::class)
class TransferSystem : TransferInterface {
    @Inject
    private lateinit var transferRepository: TransferRepository

    override fun addTransferFrom(player: String, reason: String, collectionType: String, collection: CardCollection) {
        if (collection.all.iterator().hasNext()) {
            transferRepository.addTransfer(player, reason, false, collectionType, "from", serializeCollection(collection))
        }
    }

    override fun addTransferTo(player: String, reason: String, notifyPlayer: Boolean, collectionType: String, collection: CardCollection) {
        if (collection.all.iterator().hasNext()) {
            transferRepository.addTransfer(player, reason, notifyPlayer, collectionType, "to", serializeCollection(collection))
        }
    }

    override fun hasUnnotifiedTransfers(player: String): Boolean {
        return transferRepository.hasUnnotifiedTransfers(player)
    }

    override fun consumeUnnotifiedTransfers(player: String): Map<String, CardCollection> {
        return transferRepository.consumeUnnotifiedTransfers(player).mapValues {
            val resultCollection = DefaultCardCollection()
            it.value.forEach { collectionText ->
                val collection: CardCollection = deserializeCollection(collectionText)
                collection.all.forEach { collectionItem ->
                    resultCollection.addItem(collectionItem.product, collectionItem.count)
                }
            }
            resultCollection
        }
    }

    private fun serializeCollection(collection: CardCollection): String {
        return collection.all.map { item ->
            "${item.count}x${item.product}"
        }.mergeTexts(',')
    }

    private fun deserializeCollection(collectionText: String): CardCollection {
        val result = DefaultCardCollection()
        collectionText.splitText(',').forEach { entry ->
            if (entry.isNotBlank()) {
                val split = entry.splitText('x', 2)
                result.addItem(split[1], split[0].toInt())
            }
        }
        return result
    }
}