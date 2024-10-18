package com.gempukku.server.chat

interface ChatStream {
    fun messageReceived(chatMessage: ChatMessage)

    fun playerJoined(playerId: String)

    fun playerLeft(playerId: String)

    fun chatClosed()
}
