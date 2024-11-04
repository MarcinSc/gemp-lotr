package com.gempukku.server.chat

import com.gempukku.server.chat.polling.legacy.LegacyChatEventSinkProducer

fun createChatSystems(): List<Any> {
    return listOf(
        // Responsible for chat server and its API
        ChatSystem(),
        ChatApiSystem(),
        LegacyChatEventSinkProducer(),
    )
}
