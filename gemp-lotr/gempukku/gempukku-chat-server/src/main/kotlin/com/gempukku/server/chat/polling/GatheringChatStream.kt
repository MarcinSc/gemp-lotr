package com.gempukku.server.chat.polling

import com.gempukku.server.chat.ChatMessage
import com.gempukku.server.chat.ChatStream
import com.gempukku.server.polling.GatheringStream

class GatheringChatStream : ChatStream {
    val gatheringStream: GatheringStream<ChatEvent> = GatheringStream()

    override fun messageReceived(chatMessage: ChatMessage) {
        gatheringStream.addEvent(ChatMessageEvent(chatMessage))
    }

    override fun playerJoined(playerId: String) {
        gatheringStream.addEvent(ChatJoinedEvent(playerId))
    }

    override fun playerParted(playerId: String) {
        gatheringStream.addEvent(ChatPartedEvent(playerId))
    }

    override fun chatClosed() {
        gatheringStream.setClosed()
    }
}