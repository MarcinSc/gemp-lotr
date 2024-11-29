package org.ccgemp.transfer

import org.ccgemp.collection.GempCollection

interface TransferInterface {
    fun hasUnnotifiedTransfers(player: String): Boolean

    fun consumeUnnotifiedTransfers(player: String): Map<String, GempCollection>
}
