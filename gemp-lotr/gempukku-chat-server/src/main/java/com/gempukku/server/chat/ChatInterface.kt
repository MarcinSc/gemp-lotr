package com.gempukku.server.chat

import java.util.function.Predicate

interface ChatInterface {
    fun createChatRoom(
        roomName: String,
        allowIncognito: Boolean,
        commands: Map<String, ChatCommandCallback>,
        welcomeMessage: String? = null,
        userPredicate: Predicate<String> = Predicate<String> { true }
    ): Runnable?

    fun joinUser(roomName: String, playerId: String, admin: Boolean, chatStream: ChatStream): Runnable?

    fun setIncognito(roomName: String, playerId: String, incognito: Boolean)

    fun sendMessage(roomName: String, playerId: String, message: String, admin: Boolean)

    fun sendToUser(roomName: String, from: String, to: String, message: String, admin: Boolean)
}