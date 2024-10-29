package com.gempukku.server

import org.w3c.dom.Document
import java.io.File

interface ResponseWriter {
    fun writeError(
        status: Int,
        headersMap: Map<String, String>? = null,
    )

    fun writeFile(
        file: File,
        headersMap: Map<String, String>? = null,
    )

    fun writeHtmlResponse(
        html: String,
        headersMap: Map<String, String>? = null,
    )

    fun writeJsonResponse(
        json: String,
        headersMap: Map<String, String>? = null,
    )

    fun writeByteResponse(
        bytes: ByteArray,
        headersMap: Map<String, String>? = null,
    )

    fun writeXmlResponse(
        document: Document?,
        headersMap: Map<String, String>? = null,
    )
}
