package org.ccgemp.transfer.renderer

import com.gempukku.server.ResponseWriter
import org.ccgemp.collection.GempCollection

interface TransferModelRenderer {
    fun renderGetDelivery(player: String, transfers: Map<String, GempCollection>, responseWriter: ResponseWriter)
}
