package org.ccgemp.lotr

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.game.DefaultAdventureLibrary
import com.gempukku.lotro.game.LotroCardBlueprintLibrary
import com.gempukku.lotro.game.formats.LotroFormatLibrary

@Exposes(LegacyObjectsProvider::class)
class DefaultLegacyObjectProvider : LegacyObjectsProvider {
    private val adventureLibrary: DefaultAdventureLibrary = DefaultAdventureLibrary()

    override val cardLibrary: LotroCardBlueprintLibrary = LotroCardBlueprintLibrary()
    override val formatLibrary: LotroFormatLibrary = LotroFormatLibrary(adventureLibrary, cardLibrary)
}
