package org.ccgemp.transfer

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.AuthorizedApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.HttpRequest
import com.gempukku.server.ServerRequestHandler
import com.gempukku.server.ServerResponseHeaderProcessor
import org.ccgemp.transfer.renderer.TransferModelRenderer

class TransferApiSystem : AuthorizedApiSystem() {
    @Inject
    private lateinit var transferInterface: TransferInterface

    @Inject
    private lateinit var transferModelRenderer: TransferModelRenderer

    @InjectValue("server.delivery.url")
    private lateinit var deliveryUrl: String

    @InjectValue("server.delivery-notify.url")
    private lateinit var deliveryNotifyUrl: String

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
                val actAsUser = getActingAsUser(request)

                return if (transferInterface.hasUnnotifiedTransfers(actAsUser.userId)) {
                    mapOf("Delivery-Service-Package" to "true")
                } else {
                    emptyMap()
                }
            }
        }

    private fun executeGetDelivery() =
        ServerRequestHandler { request, responseWriter ->
            val actAsUser = getActingAsUser(request)

            val transfers = transferInterface.consumeUnnotifiedTransfers(actAsUser.userId)
            if (transfers.isEmpty()) {
                throw HttpProcessingException(404)
            }

            transferModelRenderer.renderGetDelivery(actAsUser.userId, transfers, responseWriter)
        }
}
