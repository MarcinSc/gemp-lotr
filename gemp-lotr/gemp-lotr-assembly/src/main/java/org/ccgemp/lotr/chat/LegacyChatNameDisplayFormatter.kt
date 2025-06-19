package org.ccgemp.lotr.chat

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.server.player.PlayerInterface

@Exposes(ChatNameDisplayFormatter::class)
class LegacyChatNameDisplayFormatter : ChatNameDisplayFormatter {
    @Inject
    private lateinit var playerInterface: PlayerInterface

    @InjectValue("roles.admin")
    private lateinit var adminRoleName: String

    @InjectValue("roles.leagueAdmin")
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
