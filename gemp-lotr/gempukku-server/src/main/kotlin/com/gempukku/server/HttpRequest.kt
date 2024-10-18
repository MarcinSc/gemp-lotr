package com.gempukku.server

interface HttpRequest {
    val method: HttpMethod
    fun getCookie(cookieName: String): String?
    fun getQueryParameter(parameterName: String): String?
    fun getFormParameter(parameterName: String): String?
    fun getFormParameters(parameterName: String): List<String>
}