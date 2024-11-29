package org.ccgemp.lotr.transfer

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.ResponseWriter
import org.ccgemp.collection.GempCollection
import org.ccgemp.lotr.collection.CollectionContentsSerializer
import org.ccgemp.transfer.renderer.TransferModelRenderer
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(TransferModelRenderer::class)
class LegacyXmlTransferModelRenderer : TransferModelRenderer {
    @Inject
    private lateinit var collectionContentsSerializer: CollectionContentsSerializer

    override fun renderGetDelivery(player: String, transfers: Map<String, GempCollection>, responseWriter: ResponseWriter) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()

        val deliveryElem = doc.createElement("delivery")

        transfers.forEach {
            val collection = collectionContentsSerializer.serializeCollectionToXml(doc, it.value)
            collection.setAttribute("type", it.key)
            deliveryElem.appendChild(collection)
        }

        doc.appendChild(deliveryElem)

        responseWriter.writeXmlResponse(doc)
    }
}
