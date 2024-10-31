package com.gempukku.context.resource

import java.io.InputStream

class FileClasspathResource(
    private val path: String,
) : FileResource {
    override fun createInputStream(): InputStream? {
        return FileClasspathResource::class.java.getResourceAsStream(path)
    }

    override fun describe(): String {
        return "Classpath file: $path"
    }
}
