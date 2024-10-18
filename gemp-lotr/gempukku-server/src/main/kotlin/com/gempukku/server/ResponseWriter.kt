package com.gempukku.server

import org.w3c.dom.Document
import java.io.File

interface ResponseWriter {
    fun writeError(status: Int)
    fun writeError(status: Int, headers: Map<String, String>?)

    fun writeFile(file: File, headers: Map<String, String>?)

    fun writeHtmlResponse(html: String)
    fun writeJsonResponse(json: String)

    fun writeByteResponse(bytes: ByteArray, headers: Map<String, String>?)

    fun writeXmlResponse(document: Document)

    fun writeXmlResponse(document: Document, headers: Map<String, String>?)
}
