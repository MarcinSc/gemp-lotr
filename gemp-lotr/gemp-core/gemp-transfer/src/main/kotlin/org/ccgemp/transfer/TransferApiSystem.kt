package org.ccgemp.transfer

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.ApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.ServerRequestHandler
import com.gempukku.server.ServerResponseHeaderProcessor
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.UserRolesProvider
import com.gempukku.server.login.getActingAsUser
import org.ccgemp.common.CollectionContentsSerializer
import javax.xml.parsers.DocumentBuilderFactory

class TransferApiSystem : ApiSystem() {
    @Inject
    private lateinit var transferInterface: TransferInterface

    @Inject
    private lateinit var collectionContentsSerializer: CollectionContentsSerializer

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @Inject
    private lateinit var userRolesProvider: UserRolesProvider

    @InjectValue("server.delivery.url")
    private lateinit var deliveryUrl: String

    @InjectValue("server.delivery-notify.url")
    private lateinit var deliveryNotifyUrl: String

    @InjectValue("roles.admin")
    private lateinit var adminRole: String

    @InjectValue("parameterNames.actAsParameter")
    private lateinit var actAsParameter: String

    override fun registerAPIs(): List<Registration> {
        return listOf(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$deliveryUrl$",
                executeGetDelivery(),
            ),
            server.registerResponseHeadersProcessor(
                HttpMethod.GET,
                "^$deliveryNotifyUrl$",
                checkForDelivery(),
            ),
        )
    }

    private fun checkForDelivery() =
        object : ServerResponseHeaderProcessor {
            override fun getExtraHeaders(request: HttpRequest): Map<String, String> {
                val actAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)

                return if (transferInterface.hasUnnotifiedTransfers(actAsUser.userId)) {
                    mapOf("Delivery-Service-Package" to "true")
                } else {
                    emptyMap()
                }
            }
        }

    private fun executeGetDelivery() =
        ServerRequestHandler { request, responseWriter ->
            val actAsUser = getActingAsUser(loggedUserInterface, userRolesProvider, request, adminRole, actAsParameter)

            val transfers = transferInterface.consumeUnnotifiedTransfers(actAsUser.userId)
            if (transfers.isEmpty()) {
                throw HttpProcessingException(404)
            }

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
