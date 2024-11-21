package org.ccgemp.collection

import org.ccgemp.common.CardCollection

data class CollectionChange(
    val notify: Boolean,
    val reason: String,
    val collection: CardCollection,
)
