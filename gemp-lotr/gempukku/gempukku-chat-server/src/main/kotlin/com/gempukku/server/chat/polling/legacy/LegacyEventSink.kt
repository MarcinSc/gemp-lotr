package com.gempukku.server.chat.polling.legacy

import com.gempukku.server.ResponseWriter
import com.gempukku.server.chat.ChatInterface
import com.gempukku.server.chat.ChatMessage
import com.gempukku.server.chat.ChatNameDisplayFormatter
import com.gempukku.server.chat.polling.ChatEvent
import com.gempukku.server.chat.polling.ChatEventVisitor
import com.gempukku.server.polling.EventSink
import org.w3c.dom.Element
import java.util.TreeSet
import javax.xml.parsers.DocumentBuilderFactory

class LegacyEventSink(
    private val room: String,
    private val pollId: String,
    private val chatInterface: ChatInterface,
    private val chatNameDisplayFormatter: ChatNameDisplayFormatter?,
    private val responseWriter: ResponseWriter,
) : EventSink<ChatEvent> {
    override fun processEventsAndClose(events: List<ChatEvent>) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()

        val chatElem: Element = doc.createElement("chat")
        chatElem.setAttribute("roomName", room)
        chatElem.setAttribute("pollId", pollId)
        doc.appendChild(chatElem)

        val xmlVisitor =
            object : ChatEventVisitor {
                override fun visitChatMessage(chatMessage: ChatMessage) {
                    val message: Element = doc.createElement("message")
                    message.setAttribute("from", chatMessage.from)
                    message.setAttribute("date", chatMessage.date.time.toString())
                    message.appendChild(doc.createTextNode(chatMessage.message))
                    chatElem.appendChild(message)
                }
            }
        events.forEach {
            it.visit(xmlVisitor)
        }

        val users: MutableSet<String> = TreeSet<String>(CaseInsensitiveStringComparator())
        chatInterface.getUserList(room).forEach {
            users.add(formatPlayerNameForChatList(it))
        }
        for (userInRoom in users) {
            val user: Element = doc.createElement("user")
            user.appendChild(doc.createTextNode(userInRoom))
            chatElem.appendChild(user)
        }

        responseWriter.writeXmlResponse(doc)
    }

    private class CaseInsensitiveStringComparator : Comparator<String> {
        override fun compare(
            o1: String,
            o2: String,
        ): Int = o1.compareTo(o2, true)
    }

    private fun formatPlayerNameForChatList(userInRoom: String): String =
        chatNameDisplayFormatter?.formatNameDisplay(userInRoom) ?: userInRoom
}
