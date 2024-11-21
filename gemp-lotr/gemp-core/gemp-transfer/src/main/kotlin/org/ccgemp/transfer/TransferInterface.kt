package org.ccgemp.transfer

import org.ccgemp.common.CardCollection

interface TransferInterface {
    fun addTransferTo(player: String, reason: String, notifyPlayer: Boolean, collectionName: String, collection: CardCollection)
    fun addTransferFrom(player: String, reason: String, collectionName: String, collection: CardCollection)
    fun hasUnnotifiedPackages(player: String?): Boolean
    fun consumeUnnotifiedPackages(player: String?): Map<String, CardCollection>
}