package org.ccgemp.lotr

import com.gempukku.lotro.game.LotroCardBlueprintLibrary
import com.gempukku.lotro.game.formats.LotroFormatLibrary
import com.gempukku.lotro.packs.ProductLibrary

interface LegacyObjectsProvider {
    val formatLibrary: LotroFormatLibrary
    val cardLibrary: LotroCardBlueprintLibrary
    val productLibrary: ProductLibrary
    val draftLibrary: SoloDraftLibrary
}
