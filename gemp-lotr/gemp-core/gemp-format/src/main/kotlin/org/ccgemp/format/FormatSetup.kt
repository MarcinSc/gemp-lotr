package org.ccgemp.format

fun <Format> createFormatSystems(gempFormats: GempFormats<Format>): List<Any> {
    return listOf(
        FormatApiSystem(),
        gempFormats,
    )
}
