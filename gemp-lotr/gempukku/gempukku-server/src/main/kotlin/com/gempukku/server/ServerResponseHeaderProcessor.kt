package com.gempukku.server

interface ServerResponseHeaderProcessor {
    fun getExtraHeaders(request: HttpRequest): Map<String, String>
}