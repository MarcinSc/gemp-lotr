package org.ccgemp.format

import org.ccgemp.format.renderer.FormatModelRenderer

fun <Format> createFormatSystems(gempFormats: GempFormats<Format>, formatModelRenderer: FormatModelRenderer<Format>): List<Any> {
    return listOf(
        FormatApiSystem<Format>(),
        gempFormats,
        formatModelRenderer,
    )
}
