package org.ccgemp.collection

import org.ccgemp.common.GempCollection

data class CollectionChange(
    val notify: Boolean,
    val reason: String,
    val collection: GempCollection,
)
