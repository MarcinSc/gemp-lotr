package org.ccgemp.collection

interface TransferObserver {
    fun transferredTo(
        player: String,
        reason: String,
        notifyPlayer: Boolean,
        collectionType: String,
        collection: GempCollection,
    )

    fun transferredFrom(
        player: String,
        reason: String,
        collectionType: String,
        collection: GempCollection,
    )
}
