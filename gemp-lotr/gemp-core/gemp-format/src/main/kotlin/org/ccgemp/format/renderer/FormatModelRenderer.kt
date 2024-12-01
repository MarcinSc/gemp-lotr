package org.ccgemp.format.renderer

import com.gempukku.server.ResponseWriter

interface FormatModelRenderer<Format> {
    fun renderAllFormats(formats: List<Format>, responseWriter: ResponseWriter)
}
