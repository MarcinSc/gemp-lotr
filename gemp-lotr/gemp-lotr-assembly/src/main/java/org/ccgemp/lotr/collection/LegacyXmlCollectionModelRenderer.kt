package org.ccgemp.lotr.collection

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.ResponseWriter
import org.ccgemp.collection.CollectionType
import org.ccgemp.collection.GempCollection
import org.ccgemp.collection.GempCollectionItem
import org.ccgemp.collection.renderer.CollectionModelRenderer
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(CollectionModelRenderer::class)
class LegacyXmlCollectionModelRenderer : CollectionModelRenderer {
    @Inject
    private lateinit var collectionContentsSerializer: CollectionContentsSerializer

    override fun renderGetCollectionTypes(player: String, playerCollectionTypes: List<CollectionType>, responseWriter: ResponseWriter) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()

        val collectionsElem = doc.createElement("collections")

        playerCollectionTypes.forEach { collectionType ->
            val collectionElem = doc.createElement("collection")
            collectionElem.setAttribute("type", collectionType.type)
            collectionElem.setAttribute("name", collectionType.name)
            collectionElem.setAttribute("format", collectionType.format)
            collectionsElem.appendChild(collectionElem)
        }
        doc.appendChild(collectionsElem)

        responseWriter.writeXmlResponse(doc)
    }

    override fun renderOpenPack(player: String, packContents: GempCollection, responseWriter: ResponseWriter) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()

        doc.appendChild(collectionContentsSerializer.serializeCollectionToXml(doc, packContents))

        responseWriter.writeXmlResponse(doc)
    }

    override fun renderGetCollection(
        player: String,
        filteredResult: List<GempCollectionItem>,
        start: Int,
        count: Int,
        responseWriter: ResponseWriter,
    ) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()

        doc.appendChild(collectionContentsSerializer.serializeCardListToXml(doc, filteredResult, start, count))

        responseWriter.writeXmlResponse(doc)
    }
}
