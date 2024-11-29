package org.ccgemp.transfer

import org.ccgemp.transfer.renderer.JsonTransferModelRenderer
import org.ccgemp.transfer.renderer.TransferModelRenderer

fun createTransferSystems(transferModelRenderer: TransferModelRenderer = JsonTransferModelRenderer()): List<Any> {
    return listOf(
        TransferSystem(),
        DbTransferRepository(),
        TransferApiSystem(),
        transferModelRenderer,
    )
}
