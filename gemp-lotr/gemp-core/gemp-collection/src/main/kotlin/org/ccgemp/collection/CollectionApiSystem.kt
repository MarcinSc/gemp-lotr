package org.ccgemp.collection

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.ApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.ResponseWriter
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.UserRolesProvider
import com.gempukku.server.login.getActingAsUser
import org.ccgemp.collection.renderer.CollectionModelRenderer

class CollectionApiSystem : ApiSystem() {
    @Inject
    private lateinit var collectionInterface: CollectionInterface

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

    @Inject
    private lateinit var filterAndSort: FilterAndSort<GempCollectionItem>

    @Inject
    private lateinit var collectionModelRenderer: CollectionModelRenderer

    @InjectValue("server.collection.urlPrefix")
    private lateinit var urlPrefix: String

    @InjectValue("roles.admin")
    private lateinit var adminRole: String

    @InjectValue("parameterNames.actAsParameter")
    private lateinit var actAsParameter: String

    override fun registerAPIs(): List<Registration> {
        return listOf(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix$",
                executeGetCollectionTypes(),
            ),
            server.registerRequestHandler(
                HttpMethod.POST,
                "^$urlPrefix/([^/]*)$",
                executeOpenPack(),
            ),
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix/([^/]*)$",
                executeGetCollection(),
            ),
        )
    }

    private fun executeGetCollectionTypes(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val actAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)

            collectionModelRenderer.renderGetCollectionTypes(actAsUser.userId, collectionInterface.getPlayerCollectionTypes(actAsUser.userId), responseWriter)
        }

    private fun executeOpenPack(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val actAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val collectionType = request.uri.substring(urlPrefix.length + 1)
            val selection = request.getParameter("selection")
            val packId = request.getParameter("pack") ?: throw HttpProcessingException(400)

            val packContents = collectionInterface.openPackInCollection(actAsUser.userId, collectionType, packId, selection) ?: throw HttpProcessingException(404)

            collectionModelRenderer.renderOpenPack(actAsUser.userId, packContents, responseWriter)
        }

    private fun executeGetCollection(): (request: HttpRequest, responseWriter: ResponseWriter) -> Unit =
        { request, responseWriter ->
            val actAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)
            val collectionType = request.uri.substring(urlPrefix.length + 1)
            val filter = request.getParameter("filter") ?: ""
            val start = request.getParameter("start")?.toInt() ?: 0
            val count = request.getParameter("count")?.toInt() ?: 10

            val collection = collectionInterface.findPlayerCollection(actAsUser.userId, collectionType) ?: throw HttpProcessingException(404)

            val filteredResult =
                filterAndSort.process(filter, null, collection.all)

            collectionModelRenderer.renderGetCollection(actAsUser.userId, filteredResult, start, count, responseWriter)
        }
}
