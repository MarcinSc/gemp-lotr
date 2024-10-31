package com.gempukku.server.chat.polling

import com.gempukku.server.chat.ChatMessage

interface ChatEventVisitor {
    fun visitChatMessage(chatMessage: ChatMessage) {}

    fun visitChatJoined(playerDisplayName: String) {}

    fun visitChatParted(playerDisplayName: String) {}
}
