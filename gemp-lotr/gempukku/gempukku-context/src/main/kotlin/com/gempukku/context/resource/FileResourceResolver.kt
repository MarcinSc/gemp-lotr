package com.gempukku.context.resource

interface FileResourceResolver {
    fun resolveFileResource(location: String): FileResource
}