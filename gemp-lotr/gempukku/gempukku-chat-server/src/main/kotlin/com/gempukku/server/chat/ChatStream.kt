package com.gempukku.server.chat

interface ChatStream {
    fun messageReceived(chatMessage: ChatMessage)

    fun playerJoined(playerId: String)

    fun playerParted(playerId: String)

    fun chatClosed()
}
