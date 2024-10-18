package com.gempukku.server.polling

import com.gempukku.server.ResponseWriter
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class XmlEventSink(
    private val rootEventsCreator: (Document) -> Element,
    private val responseWriter: ResponseWriter
) : EventSink<(Document) -> Element> {
    override fun processEventsAndClose(events: List<(Document) -> Element>) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val document = documentBuilder.newDocument()
        val root = rootEventsCreator(document)
        events.forEach {
            root.appendChild(it.invoke(document))
        }
        responseWriter.writeXmlResponse(document)
    }
}

fun createRootElement(rootName: String, pollIdParameter: String, pollId: String): (Document) -> Element {
    return {
        val root = it.createElement(rootName)
        root.setAttribute(pollIdParameter, pollId)
        it.appendChild(root)
        root
    }
}