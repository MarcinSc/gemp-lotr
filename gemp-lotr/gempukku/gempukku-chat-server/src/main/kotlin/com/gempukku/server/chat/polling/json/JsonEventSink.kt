package com.gempukku.server.chat.polling.json

import com.gempukku.server.ResponseWriter
import com.gempukku.server.chat.ChatMessage
import com.gempukku.server.chat.polling.ChatEvent
import com.gempukku.server.chat.polling.ChatEventVisitor
import com.gempukku.server.polling.EventSink
import org.hjson.JsonArray
import org.hjson.JsonObject

class JsonEventSink(
    private val room: String,
    private val pollId: String,
    private val responseWriter: ResponseWriter,
) : EventSink<ChatEvent> {
    override fun processEventsAndClose(events: List<ChatEvent>) {
        val root = JsonObject()
        root.set("roomName", room)
        root.set("pollId", pollId)

        val messages = JsonArray()
        val jsonVisitor =
            object : ChatEventVisitor {
                override fun visitChatJoined(playerDisplayName: String) {
                    val value = JsonObject()
                    value.set("type", "joined")
                    value.set("playerDisplayName", playerDisplayName)
                    messages.add(value)
                }

                override fun visitChatMessage(chatMessage: ChatMessage) {
                    val value = JsonObject()
                    value.set("type", "message")
                    value.set("date", chatMessage.date.time)
                    value.set("from", chatMessage.from)
                    value.set("message", chatMessage.message)
                    messages.add(value)
                }

                override fun visitChatParted(playerDisplayName: String) {
                    val value = JsonObject()
                    value.set("type", "parted")
                    value.set("playerDisplayName", playerDisplayName)
                    messages.add(value)
                }
            }

        events.forEach {
            it.visit(jsonVisitor)
        }
        root.set("messages", messages)

        responseWriter.writeJsonResponse(root.toString())
    }
}
