package org.ccgemp.lotr

import com.gempukku.lotro.game.LotroCardBlueprintLibrary
import com.gempukku.lotro.game.formats.LotroFormatLibrary

interface LegacyObjectsProvider {
    val formatLibrary: LotroFormatLibrary
    val cardLibrary: LotroCardBlueprintLibrary
}