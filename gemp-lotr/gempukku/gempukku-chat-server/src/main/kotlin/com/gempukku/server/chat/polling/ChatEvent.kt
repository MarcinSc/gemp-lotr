package com.gempukku.server.chat.polling

import com.gempukku.server.chat.ChatMessage

interface ChatEvent {
    fun visit(visitor: ChatEventVisitor)
}

class ChatMessageEvent(
    private val chatMessage: ChatMessage,
) : ChatEvent {
    override fun visit(visitor: ChatEventVisitor) {
        visitor.visitChatMessage(chatMessage)
    }
}

class ChatJoinedEvent(
    private val playerDisplayName: String,
) : ChatEvent {
    override fun visit(visitor: ChatEventVisitor) {
        visitor.visitChatJoined(playerDisplayName)
    }
}

class ChatPartedEvent(
    private val playerDisplayName: String,
) : ChatEvent {
    override fun visit(visitor: ChatEventVisitor) {
        visitor.visitChatParted(playerDisplayName)
    }
}
