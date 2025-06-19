package org.ccgemp.format

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.server.ApiSystem
import com.gempukku.server.HttpMethod
import com.gempukku.server.ServerRequestHandler
import org.ccgemp.format.renderer.FormatModelRenderer

class FormatApiSystem<Format> : ApiSystem() {
    @Inject
    private lateinit var gempFormats: GempFormats<Format>

    @Inject
    private lateinit var formatModelRenderer: FormatModelRenderer<Format>

    @InjectValue("server.format.urlPrefix")
    private lateinit var urlPrefix: String

    override fun registerAPIs(): List<Registration> {
        return listOf(
            server.registerRequestHandler(
                HttpMethod.GET,
                "^$urlPrefix$",
                executeGetFormats(),
            ),
        )
    }

    private fun executeGetFormats(): ServerRequestHandler =
        ServerRequestHandler { request, responseWriter ->
            formatModelRenderer.renderAllFormats(gempFormats.getAllFormats(), responseWriter)
        }
}
