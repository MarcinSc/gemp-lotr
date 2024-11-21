package org.ccgemp.transfer

fun createTransferSystems(): List<Any> {
    return listOf(
        TransferSystem(),
        DbTransferRepository(),
        TransferApiSystem(),
    )
}
