package com.gempukku.server

interface HttpRequest {
    val method: HttpMethod
    fun getCookie(cookieName: String): String?
    fun getHeader(headerName: String): String?
    fun getParameter(parameterName: String): String?
    fun getParameters(parameterName: String): List<String>
}