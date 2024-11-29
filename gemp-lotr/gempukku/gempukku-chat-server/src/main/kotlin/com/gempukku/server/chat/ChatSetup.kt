package com.gempukku.server.chat

import com.gempukku.server.chat.polling.json.JsonChatEventSinkProducer

fun createChatSystems(chatEventSinkProducer: ChatEventSinkProducer = JsonChatEventSinkProducer()): List<Any> {
    return listOfNotNull(
        // Responsible for chat server and its API
        ChatSystem(),
        ChatApiSystem(),
        chatEventSinkProducer,
    )
}
