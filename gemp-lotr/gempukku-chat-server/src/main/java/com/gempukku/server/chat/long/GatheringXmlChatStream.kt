package com.gempukku.server.chat.long

import com.gempukku.server.chat.ChatMessage
import com.gempukku.server.chat.ChatStream
import com.gempukku.server.polling.GatheringStream
import org.w3c.dom.Document
import org.w3c.dom.Element

class GatheringXmlChatStream : ChatStream {
    val gatheringStream: GatheringStream<(Document) -> Element> = GatheringStream()

    override fun messageReceived(chatMessage: ChatMessage) {
        gatheringStream.addEvent { document ->
            val message = document.createElement("message")
            message.setAttribute("from", chatMessage.from)
            message.setAttribute("date", chatMessage.date.time.toString())
            message.appendChild(document.createTextNode(chatMessage.message))
            message
        }
    }

    override fun playerJoined(playerId: String) {
        gatheringStream.addEvent { document ->
            val join = document.createElement("join")
            join.appendChild(document.createTextNode(playerId))
            join
        }
    }

    override fun playerLeft(playerId: String) {
        gatheringStream.addEvent { document ->
            val left = document.createElement("left")
            left.appendChild(document.createTextNode(playerId))
            left
        }
    }

    override fun chatClosed() {
        gatheringStream.setClosed()
    }
}