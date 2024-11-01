package com.gempukku.context.resource

import java.io.File

class DefaultFileResourceResolver : FileResourceResolver {
    private val resourceTypeHandlers: MutableMap<String, FileResourceHandler> = mutableMapOf()

    fun addFileResourceHandler(type: String, handler: FileResourceHandler) {
        resourceTypeHandlers[type.lowercase()] = handler
    }

    fun removeFileResourceHandler(type: String) {
        resourceTypeHandlers.remove(type.lowercase())
    }

    override fun resolveFileResource(location: String): FileResource {
        val type = location.substring(0, location.indexOf(':')).lowercase()
        val value = location.substring(location.indexOf(':') + 1)
        val handler = resourceTypeHandlers[type] ?: throw IllegalArgumentException("No handler for type $type")
        return handler.createFileResource(value)
    }
}

fun createDefaultFileResourceResolver(): FileResourceResolver {
    val result = DefaultFileResourceResolver()
    result.addFileResourceHandler(
        "file",
        object : FileResourceHandler {
            override fun createFileResource(value: String): FileResource {
                return FileSystemResource(File(value))
            }
        },
    )
    result.addFileResourceHandler(
        "classpath",
        object : FileResourceHandler {
            override fun createFileResource(value: String): FileResource {
                return FileClasspathResource(value)
            }
        },
    )
    return result
}
