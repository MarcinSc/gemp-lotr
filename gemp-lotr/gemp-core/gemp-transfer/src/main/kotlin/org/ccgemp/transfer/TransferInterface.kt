package org.ccgemp.transfer

import org.ccgemp.collection.GempCollection

interface TransferInterface {
    fun addTransferTo(
        player: String,
        reason: String,
        notifyPlayer: Boolean,
        collectionType: String,
        collection: GempCollection,
    )

    fun addTransferFrom(
        player: String,
        reason: String,
        collectionType: String,
        collection: GempCollection,
    )

    fun hasUnnotifiedTransfers(player: String): Boolean

    fun consumeUnnotifiedTransfers(player: String): Map<String, GempCollection>
}
