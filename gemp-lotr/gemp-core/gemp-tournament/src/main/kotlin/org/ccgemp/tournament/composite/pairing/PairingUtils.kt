package org.ccgemp.tournament.composite.pairing

import com.google.common.math.IntMath
import org.ccgemp.tournament.composite.standing.PlayerStanding
import kotlin.math.ceil
import kotlin.math.log2

fun pairBracket(
    standings: List<PlayerStanding>,
    players: List<String>,
    pairings: MutableSet<Pair<String, String>>,
    byes: MutableSet<String>,
) {
    val notDroppedInStandingsOrder = standings.map { it.player }.filter { it in players }
    val bracketSize = IntMath.pow(2, ceil(log2(players.size.toFloat())).toInt())
    (0..bracketSize / 2).forEach { bracketNo ->
        val firstPlayer = notDroppedInStandingsOrder[bracketNo]
        val secondPlayer = notDroppedInStandingsOrder.getOrNull(bracketSize - bracketNo - 1)
        if (secondPlayer == null) {
            byes.add(firstPlayer)
        } else {
            pairings.add(Pair(firstPlayer, secondPlayer))
        }
    }
}
