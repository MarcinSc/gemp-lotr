package org.ccgemp.collection

import com.gempukku.server.login.LoggedUser

interface CollectionTypeProvider {
    fun getCollectionTypes(player: String): List<CollectionType>
}
