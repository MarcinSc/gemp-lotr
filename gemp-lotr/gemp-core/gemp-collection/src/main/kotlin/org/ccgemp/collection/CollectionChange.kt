package org.ccgemp.collection

data class CollectionChange(
    val notify: Boolean,
    val reason: String,
    val collection: GempCollection,
)
