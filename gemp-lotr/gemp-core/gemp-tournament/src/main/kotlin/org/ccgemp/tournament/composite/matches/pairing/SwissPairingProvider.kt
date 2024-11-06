package org.ccgemp.tournament.composite.matches.pairing

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentPlayer
import org.ccgemp.tournament.composite.matches.standing.PlayerStanding
import org.ccgemp.tournament.composite.matches.standing.Standings
import org.ccgemp.tournament.composite.matches.standing.StandingsConfig
import org.ccgemp.tournament.composite.matches.standing.TournamentStandingsRegistry
import java.util.concurrent.ThreadLocalRandom

@Exposes(LifecycleObserver::class)
class SwissPairingProvider : LifecycleObserver {
    @Inject
    private lateinit var registry: TournamentPairingRegistry

    @Inject
    private lateinit var standingsRegistry: TournamentStandingsRegistry

    override fun afterContextStartup() {
        val pairingProvider: (JsonWithConfig<PairingConfig>) -> Pairing = {
            SwissPairing(standingsRegistry.create(JsonWithConfig(it.json.get("standings").asObject(), StandingsConfig(it.config.tournamentId))))
        }
        registry.register("swiss", pairingProvider)
    }
}

class SwissPairing(
    private val standings: Standings,
) : Pairing {
    override fun isReady(round: Int): Boolean {
        return true
    }

    override fun createPairings(
        round: Int,
        players: List<TournamentParticipant>,
        matches: List<TournamentMatch>,
        pairingGroups: Map<Int, String>,
        byeGroups: Map<Int, String>,
    ): RoundPairing? {
        val standings = standings.createStandings(round - 1, players, matches)

        val notByeMatches = matches.filter { !it.bye }
        val notDroppedPlayers = players.filter { !it.dropped }.map { it.player }
        val notDroppedStandings = standings.filter { notDroppedPlayers.contains(it.name) }

        val maxNumberOfPoints = notDroppedStandings.maxOf { it.points }

        val playersGroupedByBracket = groupPlayersByBracket(maxNumberOfPoints, notDroppedStandings)

        playersGroupedByBracket.forEach {
            it.shuffle(ThreadLocalRandom.current())
        }

        val byeGroup = byeGroups[round]
        val pairingGroup = pairingGroups[round]

        // Get all players that got a bye in the same bye group
        val playersWithByes = matches.filter { it.bye && byeGroups[it.round] == byeGroup }.mapNotNullTo(mutableSetOf()) { it.winner }
        val previouslyPaired =
            notDroppedPlayers.associate { player ->
                val opponents = notByeMatches.filter { it.hasPlayer(player) && pairingGroups[it.round] == pairingGroup }.mapNotNullTo(mutableSetOf()) { it.getOpponent(player) }
                player to opponents
            }

        val pairingResults = mutableMapOf<String, String>()
        val byeResults = mutableSetOf<String>()

        val success: Boolean =
            tryPairBracketAndFurther(
                0,
                mutableSetOf(),
                mutableSetOf(),
                playersGroupedByBracket,
                playersWithByes,
                previouslyPaired,
                pairingResults,
                byeResults,
            )

        // Managed to pair with this carry over count - proceed with the pairings
        if (success) {
            return RoundPairing(pairingResults.mapTo(mutableSetOf()) { it.key to it.value }, byeResults)
        }

        // We can't pair, just finish the tournament
        return null
    }

    private fun tryPairBracketAndFurther(
        bracketIndex: Int,
        carryOverPlayers: MutableSet<String>,
        carryOverFromThisBracket: MutableSet<String>,
        playersGroupedByBracket: List<MutableList<String>>,
        playersWithByes: Set<String>,
        previouslyPaired: Map<String, Set<String>>,
        pairingsResult: MutableMap<String, String>,
        byes: MutableSet<String>,
    ): Boolean {
        val playersInBracket = playersGroupedByBracket[bracketIndex]

        // First try to pair carried over players
        while (carryOverPlayers.isNotEmpty()) {
            val firstCarryOver = carryOverPlayers.iterator().next()
            carryOverPlayers.remove(firstCarryOver)

            for (index in playersInBracket.indices) {
                val player = playersInBracket.removeAt(index)
                if (!previouslyPaired[firstCarryOver]!!.contains(player)) {
                    // This might be a good pairing
                    pairingsResult[firstCarryOver] = player
                    // Lets give it a try
                    val success =
                        tryPairBracketAndFurther(
                            bracketIndex,
                            carryOverPlayers,
                            carryOverFromThisBracket,
                            playersGroupedByBracket,
                            playersWithByes,
                            previouslyPaired,
                            pairingsResult,
                            byes,
                        )
                    if (success) {
                        return true
                    }
                    // Naah, it didn't work out
                    pairingsResult.remove(firstCarryOver)
                }
                playersInBracket.add(index, player)
            }

            carryOverFromThisBracket.add(firstCarryOver)
        }

        if (playersInBracket.size > 1) {
            // Pair whatever we manage within a bracket
            for (index in 0 until playersInBracket.size - 1) {
                val firstPlayer = playersInBracket.removeAt(index)
                for (index2 in index until playersInBracket.size) {
                    val secondPlayer = playersInBracket.removeAt(index2)
                    if (!previouslyPaired[firstPlayer]!!.contains(secondPlayer)) {
                        // This pairing might work
                        pairingsResult[firstPlayer] = secondPlayer
                        // Lets give it a try
                        val success =
                            tryPairBracketAndFurther(
                                bracketIndex,
                                mutableSetOf(),
                                carryOverFromThisBracket,
                                playersGroupedByBracket,
                                playersWithByes,
                                previouslyPaired,
                                pairingsResult,
                                byes,
                            )
                        if (success) {
                            return true
                        }
                        // Naah, it didn't work out
                        pairingsResult.remove(firstPlayer)
                    }
                    playersInBracket.add(index2, secondPlayer)
                }
                playersInBracket.add(index, firstPlayer)
            }
        }

        // We have to go to next bracket
        if (bracketIndex + 1 < playersGroupedByBracket.size) {
            // Remaining players can't be paired within this bracket
            val carryOverForNextBracket: MutableSet<String> = HashSet(carryOverFromThisBracket)
            carryOverForNextBracket.addAll(playersInBracket)

            return tryPairBracketAndFurther(
                bracketIndex + 1,
                carryOverForNextBracket,
                HashSet(),
                playersGroupedByBracket,
                playersWithByes,
                previouslyPaired,
                pairingsResult,
                byes,
            )
        } else {
            // There is no more brackets left, whatever is left, has to get a bye
            val leftoverPlayers: MutableSet<String> = HashSet(carryOverFromThisBracket)
            leftoverPlayers.addAll(playersInBracket)

            // We only accept one bye
            val playersLeftWithoutPair = leftoverPlayers.size
            when (playersLeftWithoutPair) {
                0 -> return true
                1 -> {
                    val lastPlayer = leftoverPlayers.iterator().next()
                    if (playersWithByes.contains(lastPlayer)) {
                        // The last remaining player already has a bye
                        return false
                    } else {
                        byes.add(lastPlayer)
                        return true
                    }
                }

                else -> return false
            }
        }
    }

    private fun groupPlayersByBracket(maxNumberOfPoints: Int, standings: List<PlayerStanding>): List<MutableList<String>> {
        val playersByPoints = arrayOfNulls<MutableList<String>>(maxNumberOfPoints + 1)
        for (currentStanding in standings) {
            val playerName: String = currentStanding.name
            val points = currentStanding.points
            var playersByPoint = playersByPoints[maxNumberOfPoints - points]
            if (playersByPoint == null) {
                playersByPoint = mutableListOf()
                playersByPoints[maxNumberOfPoints - points] = playersByPoint
            }
            playersByPoint.add(playerName)
        }

        val result = mutableListOf<MutableList<String>>()
        for (playersByPoint in playersByPoints) {
            if (playersByPoint != null) {
                result.add(playersByPoint)
            }
        }

        return result
    }
}
