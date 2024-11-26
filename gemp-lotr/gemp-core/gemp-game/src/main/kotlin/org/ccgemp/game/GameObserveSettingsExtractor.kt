package org.ccgemp.game

import com.gempukku.server.HttpRequest

interface GameObserveSettingsExtractor<ObserveSettings> {
    fun extractSettings(request: HttpRequest): ObserveSettings
}
