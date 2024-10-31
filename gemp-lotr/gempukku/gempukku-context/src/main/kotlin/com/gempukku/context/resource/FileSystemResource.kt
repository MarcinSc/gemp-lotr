package com.gempukku.context.resource

import java.io.File
import java.io.InputStream

class FileSystemResource(
    private val file: File,
) : FileResource {
    override fun createInputStream(): InputStream? {
        return file.takeIf { it.isFile }?.inputStream()
    }

    override fun describe(): String {
        return "File: ${file.absolutePath}"
    }
}
