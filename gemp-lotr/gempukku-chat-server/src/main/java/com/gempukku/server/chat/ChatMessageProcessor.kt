package com.gempukku.server.chat

interface ChatMessageProcessor {
    fun processMessage(message: String): String
}