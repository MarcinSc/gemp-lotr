package com.gempukku.server.chat

import java.util.Date
import java.util.LinkedList
import java.util.Locale
import java.util.function.Predicate

const val MAX_MESSAGE_HISTORY_COUNT = 500

class ChatRoom(
    private val allowIncognito: Boolean,
    private val commands: Map<String, ChatCommandCallback>,
    private val welcomeMessage: String?,
    private val userPredicate: Predicate<String>,
) {
    private val openChatStreams: MutableSet<ChatStreamConfig> = mutableSetOf()
    private val chatHistory: LinkedList<ChatMessage> = LinkedList()
    private val incognitoPlayers: MutableSet<String> = mutableSetOf()

    fun close() {
        openChatStreams.forEach {
            it.chatStream.chatClosed()
        }
        openChatStreams.clear()
    }

    private fun canAccessRoom(playerId: String, admin: Boolean): Boolean = admin || userPredicate.test(playerId)

    private fun playerIsInRoom(playerId: String): Boolean =
        !incognitoPlayers.contains(playerId) &&
            openChatStreams.any {
                it.playerId == playerId
            }

    fun setIncognito(playerId: String, incognito: Boolean) {
        if (allowIncognito) {
            val wasInRoom = playerIsInRoom(playerId)
            if (incognito) {
                incognitoPlayers.add(playerId)
            } else {
                incognitoPlayers.remove(playerId)
            }
            val isInRoom = playerIsInRoom(playerId)

            if (wasInRoom && !isInRoom) {
                openChatStreams.forEach {
                    it.chatStream.playerParted(playerId)
                }
            } else if (!wasInRoom && isInRoom) {
                openChatStreams.forEach {
                    it.chatStream.playerJoined(playerId)
                }
            }
        }
    }

    fun joinUser(
        playerId: String,
        admin: Boolean,
        allowedAuthors: Predicate<String>,
        chatStream: ChatStream,
    ): Runnable? {
        if (!canAccessRoom(playerId, admin)) {
            return null
        }

        val chatStreamConfig = ChatStreamConfig(playerId, admin, allowedAuthors, chatStream)
        if (!playerIsInRoom(playerId)) {
            openChatStreams.forEach {
                it.chatStream.playerJoined(playerId)
            }
        }

        openChatStreams.add(chatStreamConfig)

        // Send information about all players in room
        openChatStreams.map { it.playerId }.toSet().forEach {
            if (!incognitoPlayers.contains(it)) {
                chatStream.playerJoined(it)
            }
        }
        // Send chat history
        chatHistory.forEach {
            chatStream.messageReceived(it)
        }
        // Send welcome message
        if (welcomeMessage != null) {
            chatStream.messageReceived(ChatMessage(Date(), "System", welcomeMessage, false))
        }

        return Runnable {
            val playerWasInRoom = playerIsInRoom(playerId)
            openChatStreams.remove(chatStreamConfig)
            if (!playerIsInRoom(playerId) && playerWasInRoom) {
                openChatStreams.forEach {
                    it.chatStream.playerParted(playerId)
                }
            }
        }
    }

    fun sendMessage(from: String, message: String, admin: Boolean) {
        if (canAccessRoom(from, admin)) {
            if (message.trim { it <= ' ' }.startsWith("/")) {
                processIfKnownCommand(from, message.trim { it <= ' ' }.substring(1), admin)
                return
            } else {
                val chatMessage = ChatMessage(Date(), from, message, admin)
                chatHistory.add(chatMessage)
                shrinkLastMessages()

                openChatStreams.forEach {
                    if (admin || it.allowedAuthors.test(from)) {
                        it.chatStream.messageReceived(chatMessage)
                    }
                }
            }
        }
    }

    private fun processIfKnownCommand(playerId: String, commandString: String, admin: Boolean) {
        val spaceIndex = commandString.indexOf(" ")
        val commandName: String
        var commandParameters = ""
        if (spaceIndex > -1) {
            commandName = commandString.substring(0, spaceIndex)
            commandParameters = commandString.substring(spaceIndex + 1)
        } else {
            commandName = commandString
        }
        val callbackForCommand = commands[commandName.lowercase(Locale.getDefault())] ?: commands["nocommand"]
        callbackForCommand?.commandReceived(playerId, commandParameters, admin)
    }

    fun sendToUser(
        from: String,
        to: String,
        message: String,
        admin: Boolean,
    ) {
        if (canAccessRoom(from, admin)) {
            val chatMessage = ChatMessage(Date(), from, message, admin)

            openChatStreams.forEach {
                if (it.playerId.equals(to) && (admin || it.allowedAuthors.test(from))) {
                    it.chatStream.messageReceived(chatMessage)
                }
            }
        }
    }

    fun getUsers(): List<String> {
        val result =
            openChatStreams.mapNotNullTo(mutableSetOf()) {
                it.playerId.takeIf { playerId -> !incognitoPlayers.contains(playerId) }
            }
        return result.toList()
    }

    private fun shrinkLastMessages() {
        while (chatHistory.size > MAX_MESSAGE_HISTORY_COUNT) {
            chatHistory.removeFirst()
        }
    }

    private data class ChatStreamConfig(
        val playerId: String,
        val admin: Boolean,
        val allowedAuthors: Predicate<String>,
        val chatStream: ChatStream,
    )
}
