package org.ccgemp.server.player

import org.ccgemp.server.player.admin.AdminPlayerApiSystem
import org.ccgemp.server.player.admin.AdminPlayerSystem

fun createPlayerSystems(): List<Any> {
    return listOf(
        // Responsible for player registration, login, etc.
        PlayerSystem(),
        PlayerApiSystem(),
        DbPlayerRepository(),
        // Responsible for administrating users
        AdminPlayerSystem(),
        AdminPlayerApiSystem(),
    )
}
