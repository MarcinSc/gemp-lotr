package org.ccgemp.transfer

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.*
import com.gempukku.server.login.LoggedUserInterface
import com.gempukku.server.login.getActingAsUser
import org.ccgemp.common.CollectionContentsSerializer
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(LifecycleObserver::class)
class TransferApiSystem : LifecycleObserver {
    @Inject
    private lateinit var transferInterface: TransferInterface

    @Inject
    private lateinit var collectionContentsSerializer: CollectionContentsSerializer

    @Inject
    private lateinit var server: HttpServer

    @Inject
    private lateinit var loggedUserInterface: LoggedUserInterface

    @InjectValue("server.delivery.url")
    private lateinit var deliveryUrl: String

    @InjectValue("server.delivery-notify.url")
    private lateinit var deliveryNotifyUrl: String

    @InjectValue("roles.admin")
    private lateinit var adminRole: String

    @InjectValue("parameterNames.actAsParameter")
    private lateinit var actAsParameter: String

    private val deregistration: MutableList<Runnable> = mutableListOf()

    override fun afterContextStartup() {
        deregistration.add(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$deliveryUrl$",
                executeGetDelivery(),
            ),
        )
        deregistration.add(
            server.registerResponseHeadersProcessor(
                HttpMethod.GET,
                "^$deliveryNotifyUrl$",
                checkForDelivery(),
            ),
        )
    }

    private fun checkForDelivery() =
        object: ServerResponseHeaderProcessor {
            override fun getExtraHeaders(request: HttpRequest): Map<String, String> {
                val actAsUser = getActingAsUser(loggedUserInterface, request, adminRole, actAsParameter)

                return if (transferInterface.hasUnnotifiedTransfers(actAsUser.userId)) {
                    mapOf("Delivery-Service-Package" to "true")
                } else {
                    emptyMap()
                }
            }
        }

    private fun executeGetDelivery() =
        ServerRequestHandler { request, responseWriter ->
            val actAsUser = getActingAsUser(loggedUserInterface, request, adminRole, actAsParameter)

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

    override fun beforeContextStopped() {
        deregistration.forEach {
            it.run()
        }
        deregistration.clear()
    }
}