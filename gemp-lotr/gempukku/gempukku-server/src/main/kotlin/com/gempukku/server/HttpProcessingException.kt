package com.gempukku.server

class HttpProcessingException @JvmOverloads constructor(val status: Int, override val message: String = "") :
    Exception()
