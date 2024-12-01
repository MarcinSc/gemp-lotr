package org.ccgemp.lotr

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.game.DefaultAdventureLibrary
import com.gempukku.lotro.game.LotroCardBlueprintLibrary
import com.gempukku.lotro.game.formats.LotroFormatLibrary
import com.gempukku.lotro.packs.ProductLibrary
import java.io.File

@Exposes(LegacyObjectsProvider::class)
class DefaultLegacyObjectProvider : LegacyObjectsProvider {
    private val adventureLibrary: DefaultAdventureLibrary = DefaultAdventureLibrary()

    override val cardLibrary: LotroCardBlueprintLibrary =
        LotroCardBlueprintLibrary(
            File("gemp-lotr-cards/src/main/resources/cards"),
            File("gemp-lotr-cards/src/main/resources/blueprintMapping.txt"),
            File("gemp-lotr-cards/src/main/resources/setConfig.hjson"),
            File("gemp-lotr-cards/src/main/resources/rarities"),
        )
    override val formatLibrary: LotroFormatLibrary =
        LotroFormatLibrary(
            adventureLibrary,
            cardLibrary,
            File("gemp-lotr-cards/src/main/resources/lotrFormats.hjson"),
            File("gemp-lotr-cards/src/main/resources/sealed"),
        )

    override val productLibrary: ProductLibrary =
        ProductLibrary(
            cardLibrary,
            File("gemp-lotr-cards/src/main/resources/product"),
        )

    override val draftLibrary: SoloDraftLibrary =
        DefaultSoloDraftLibrary(
            File("gemp-lotr-cards/src/main/resources/draft"),
        )
}
