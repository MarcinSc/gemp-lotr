package org.ccgemp.transfer

import org.ccgemp.common.CardCollection

interface TransferInterface {
    fun addTransferTo(player: String, reason: String, notifyPlayer: Boolean, collectionType: String, collection: CardCollection)
    fun addTransferFrom(player: String, reason: String, collectionType: String, collection: CardCollection)
    fun hasUnnotifiedTransfers(player: String): Boolean
    fun consumeUnnotifiedTransfers(player: String): Map<String, CardCollection>
}