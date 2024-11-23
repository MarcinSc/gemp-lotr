package org.ccgemp.collection

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.HttpServer
import com.gempukku.server.ResponseWriter
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.UserRolesProvider
import com.gempukku.server.login.getActingAsUser
import org.ccgemp.common.CollectionContentsSerializer
import org.ccgemp.common.GempCollectionItem
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(LifecycleObserver::class)
class CollectionApiSystem : LifecycleObserver {
    @Inject
    private lateinit var collectionInterface: CollectionInterface

    @Inject
    private lateinit var server: HttpServer

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

    @Inject
    private lateinit var filterAndSort: FilterAndSort<GempCollectionItem>

    @Inject
    private lateinit var collectionContentsSerializer: CollectionContentsSerializer

    @InjectValue("server.collection.urlPrefix")
    private lateinit var urlPrefix: String

    @InjectValue("roles.admin")
    private lateinit var adminRole: String

    @InjectValue("parameterNames.actAsParameter")
    private lateinit var actAsParameter: String

    private val deregistration: MutableList<Runnable> = mutableListOf()

    override fun afterContextStartup() {
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix$",
                executeGetCollectionTypes(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/([^/]*)$",
                executeOpenPack(),
            ),
        )
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/([^/]*)$",
                executeGetCollection(),
            ),
        )
    }

    private fun executeGetCollectionTypes(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val actAsUser = getActingAsUser(loggedUserInterface,userRolesProvider,  request, adminRole, actAsParameter)

            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()

            val doc = documentBuilder.newDocument()

            val collectionsElem = doc.createElement("collections")

            collectionInterface.getPlayerCollectionTypes(actAsUser.userId).forEach { collectionType ->
                val collectionElem = doc.createElement("collection")
                collectionElem.setAttribute("type", collectionType.type)
                collectionElem.setAttribute("name", collectionType.name)
                collectionElem.setAttribute("format", collectionType.format)
                collectionsElem.appendChild(collectionElem)
            }
            doc.appendChild(collectionsElem)

            responseWriter.writeXmlResponse(doc)
        }

    private fun executeOpenPack(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val actAsUser = getActingAsUser(loggedUserInterface,userRolesProvider,  request, adminRole, actAsParameter)
            val collectionType = request.uri.substring(urlPrefix.length + 1)
            val selection = request.getParameter("selection")
            val packId = request.getParameter("pack") ?: throw HttpProcessingException(400)

            val packContents = collectionInterface.openPackInCollection(actAsUser.userId, collectionType, packId, selection) ?: throw HttpProcessingException(404)

            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()

            val doc = documentBuilder.newDocument()

            doc.appendChild(collectionContentsSerializer.serializeCollectionToXml(doc, packContents))

            responseWriter.writeXmlResponse(doc)
        }

    private fun executeGetCollection(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val actAsUser = getActingAsUser(loggedUserInterface,userRolesProvider,  request, adminRole, actAsParameter)
            val collectionType = request.uri.substring(urlPrefix.length + 1)
            val filter = request.getParameter("filter") ?: ""
            val start = request.getParameter("start")?.toInt() ?: 0
            val count = request.getParameter("count")?.toInt() ?: 10

            val collection = collectionInterface.getPlayerCollection(actAsUser.userId, collectionType) ?: throw HttpProcessingException(404)

            val filteredResult =
                filterAndSort.process(filter, null, collection.all)

            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()

            val doc = documentBuilder.newDocument()

            doc.appendChild(collectionContentsSerializer.serializeCardListToXml(doc, filteredResult, start, count))

            responseWriter.writeXmlResponse(doc)
        }

    override fun beforeContextStopped() {
        deregistration.forEach {
            it.run()
        }
        deregistration.clear()
    }
}
