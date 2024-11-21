package org.ccgemp.transfer

interface TransferRepository {
    fun addTransfer(player: String, reason: String, notifyPlayer: Boolean, collectionType: String, direction: String, collection: String)
    fun hasUnnotifiedTransfers(player: String): Boolean
    fun consumeUnnotifiedTransfers(player: String): Map<String, List<String>>
}