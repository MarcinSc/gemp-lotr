package org.ccgemp.lotr.format

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.common.JSONDefs.FullFormatReadout
import com.gempukku.lotro.common.JSONDefs.ItemStub
import com.gempukku.lotro.game.LotroFormat
import com.gempukku.server.ResponseWriter
import com.gempukku.util.JsonUtils
import org.ccgemp.format.renderer.FormatModelRenderer
import org.ccgemp.lotr.LegacyObjectsProvider

@Exposes(FormatModelRenderer::class)
class LotrFormatModelRenderer : FormatModelRenderer<LotroFormat> {
    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    override fun renderAllFormats(formats: List<LotroFormat>, responseWriter: ResponseWriter) {
        val data = FullFormatReadout()
        data.Formats = legacyObjectsProvider.formatLibrary.allFormats.values.associate { it.code to it.Serialize() }
        data.SealedTemplates = legacyObjectsProvider.formatLibrary.GetAllSealedTemplates().values.associate { it.GetName() to it.Serialize() }
        data.DraftTemplates = legacyObjectsProvider.draftLibrary.allSoloDraftFormats.entries.associate { it.key to ItemStub(it.key, it.value) }

        responseWriter.writeJsonResponse(JsonUtils.Serialize(data))
    }
}
