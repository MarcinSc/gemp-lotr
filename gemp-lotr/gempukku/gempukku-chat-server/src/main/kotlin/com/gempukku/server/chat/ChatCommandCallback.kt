package com.gempukku.server.chat

interface ChatCommandCallback {
    fun commandReceived(from: String, parameters: String, admin: Boolean)
}