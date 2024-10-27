package org.ccgemp.lotr

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.processor.inject.InjectProperty
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.chat.ChatNameDisplayFormatter
import org.ccgemp.server.player.PlayerInterface

@Exposes(ChatNameDisplayFormatter::class)
class LegacyChatNameDisplayFormatter : ChatNameDisplayFormatter {
    @Inject
    private lateinit var playerInterface: PlayerInterface

    @InjectProperty("roles.admin")
    private lateinit var adminRoleName: String

    @InjectProperty("roles.leagueAdmin")
    private lateinit var leagueAdminRoleName: String

    override fun formatNameDisplay(playerId: String): String {
        val player = playerInterface.findPlayerByLogin(playerId)
        return when {
            player?.hasRole(adminRoleName) == true -> "* $playerId"
            player?.hasRole(leagueAdminRoleName) == true -> "+ $playerId"
            else -> playerId
        }
    }
}