package org.ccgemp.lotr.game

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.common.Phase
import com.gempukku.server.HttpRequest
import org.ccgemp.game.GameObserveSettingsExtractor

@Exposes(GameObserveSettingsExtractor::class)
class LotrGameObserveSettingsExtractor : GameObserveSettingsExtractor<Set<Phase>> {
    private val autoPassDefault = setOf(Phase.FELLOWSHIP, Phase.MANEUVER, Phase.ARCHERY, Phase.ASSIGNMENT, Phase.REGROUP)

    override fun extractSettings(request: HttpRequest): Set<Phase> {
        val autoPassPhasesCookie = request.getCookie("autoPassPhases")
        if (autoPassPhasesCookie != null) {
            val phases = autoPassPhasesCookie.split("0".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val result = mutableSetOf<Phase>()
            for (phase in phases) result.add(Phase.valueOf(phase))
            return result
        }
        val autoPassCookie = request.getCookie("autoPass")
        if (autoPassCookie != null) {
            if (autoPassCookie == "false") {
                return emptySet()
            }
        }
        return autoPassDefault
    }
}
