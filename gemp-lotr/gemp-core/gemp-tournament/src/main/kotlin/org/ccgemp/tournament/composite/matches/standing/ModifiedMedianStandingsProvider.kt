package org.ccgemp.tournament.composite.matches.standing

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.JsonWithConfig
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant

const val MEDIAN_SCORE = "medianScore"
const val CUMULATIVE_SCORE = "cumulativeScore"
const val OPPONENT_WIN_RATE = "opponentWinRate"

@Exposes(LifecycleObserver::class)
class ModifiedMedianStandingsProvider : LifecycleObserver {
    @Inject
    private lateinit var registry: TournamentStandingsRegistry

    override fun afterContextStartup() {
        val standingsProvider: (JsonWithConfig<StandingsConfig>) -> Standings = {
            ModifiedMedianStandings(
                it.json.getInt("pointsForWin", 1),
                it.json.getInt("pointsForLoss", 0),
            )
        }

        registry.register("modifiedMedian", standingsProvider)
    }
}

private class ModifiedMedianStandings(
    private val pointsForWin: Int = 1,
    private val pointsForLoss: Int = 0,
) : Standings {
    override fun createStandings(round: Int, players: List<TournamentParticipant>, matches: List<TournamentMatch>): List<PlayerStanding> {
        val playerOpponents = mutableMapOf<String, MutableList<String>>()
        val playerWinCounts = mutableMapOf<String, Int>()
        val playerLossCounts = mutableMapOf<String, Int>()
        val playerScores = mutableMapOf<String, MutableList<Int>>()

        val matchesAtRound = matches.filter { it.round <= round }

        val rounds = matchesAtRound.maxOf { it.round }

        players.map { it.player }.forEach {
            playerOpponents[it] = mutableListOf()
            playerWinCounts[it] = 0
            playerLossCounts[it] = 0
            playerScores[it] = mutableListOf()
        }

        val participants = players.map { it.player }
        val finishedNonByeMatches = matchesAtRound.filter { it.finished && !it.bye }
        val finishedMatchesByRound = matchesAtRound.filter { it.finished }.groupBy { it.round }
        val playerByes = matchesAtRound.filter { it.bye }.groupBy({ it.winner!! }, { it.round })

        for (match in finishedNonByeMatches) {
            playerOpponents[match.winner]!!.add(match.loser!!)
            playerOpponents[match.loser]!!.add(match.winner!!)
            playerWinCounts.incrementFor(match.winner)
            playerLossCounts.incrementFor(match.loser)
        }

        // used for cumulative scoring, which requires we have the game results in the order that they occurred.
        for (i in 1..rounds) {
            finishedMatchesByRound[i]?.forEach { match ->
                if (match.bye) {
                    // For the purposes of cumulative scoring, we'll count a bye as a loss only because
                    // it represents no effort, and cumulative scoring is supposed to compound higher
                    // effort wins.
                    playerScores[match.winner]!!.add(0)
                } else {
                    playerScores[match.winner]!!.add(1)
                    playerScores[match.loser]!!.add(0)
                }
            }
        }

        val standings = mutableMapOf<String, PlayerStanding>()
        for (playerName in participants) {
            val playerWins = playerWinCounts[playerName]!!
            val playerLosses = playerLossCounts[playerName]!!
            var points = playerWins * pointsForWin + playerLosses * pointsForLoss
            var gamesPlayed = playerWins + playerLosses

            val byeCount = (playerByes[playerName]?.size ?: 0)
            points += pointsForWin * byeCount
            gamesPlayed += 1 * byeCount

            /*
            The Modified Median system calculates tiebreakers as follows:

                Players with the same score who faced each other defer to the winner.
                Players with more than 50% score have only their lowest-scoring opponent's score discarded;
                Players with less than 50% score have only their highest-scoring opponent's score discarded.
                Players with exactly 50% score discard both their lowest and highest opponent score;

             */
            val opponents = playerOpponents[playerName]!!
            val oppScores = mutableListOf<Int>()
            var opponentWins = 0
            var opponentGames = 0

            for (opponent in opponents) {
                var wins = playerWinCounts[opponent]!!.toInt()
                if (playerByes.containsKey(opponent)) {
                    wins += 1
                }
                oppScores.add(wins)
                opponentWins += wins
                opponentGames += playerWinCounts[opponent]!!.toInt() + playerLossCounts[opponent]!!.toInt()
            }

            oppScores.sortWith(Comparator.reverseOrder())
            var opponentWR = 0f
            if (opponentGames != 0) {
                opponentWR = opponentWins * 1f / opponentGames
            }

            // List of opponent scores is now sorted such that the first entry is the highest score, and the last entry
            // is the lowest score.  We will drop one or more of those positions based on the player's performance:
            if (gamesPlayed > 0) {
                if (playerWins == playerLosses) { // i.e. that player has a 50% win rate; this eliminates floating point comparisons
                    if (oppScores.size > 1) {
                        oppScores.removeLast()
                    }

                    if (oppScores.size > 1) {
                        oppScores.removeFirst()
                    }
                } else if (playerWins > playerLosses) {
                    if (oppScores.size > 1) {
                        oppScores.removeLast()
                    }
                } else { // playerWins < playerLosses
                    if (oppScores.size > 1) {
                        oppScores.removeFirst()
                    }
                }
            }

            var median = 0
            if (oppScores.isNotEmpty()) {
                median = oppScores.sum()
            }

            var cumulative = 0
            var lastStep = 0
            if (gamesPlayed > 0) {
                for (score in playerScores[playerName]!!) {
                    lastStep += score
                    cumulative += lastStep
                }
            }

            val stats = mutableMapOf<String, Number>()
            stats[MEDIAN_SCORE] = median
            stats[OPPONENT_WIN_RATE] = opponentWR
            stats[CUMULATIVE_SCORE] = cumulative

            standings[playerName] = PlayerStanding(playerName, 0, points, stats)
        }

        val comparator = comparator(finishedNonByeMatches)

        val tempStandings = standings.values.sortedWith(comparator)

        var standing = 0
        var position = 1
        var lastStanding: PlayerStanding? = null
        val result = mutableListOf<PlayerStanding>()
        for (eventStanding in tempStandings) {
            if (lastStanding == null || comparator.compare(eventStanding, lastStanding) != 0) {
                standing = position
            }
            val newStanding = PlayerStanding(eventStanding.name, standing, eventStanding.points, eventStanding.stats)
            result.add(newStanding)
            position++
            lastStanding = eventStanding
        }

        return result
    }

    private fun comparator(matches: List<TournamentMatch>) =
        Comparator.comparingInt { x: PlayerStanding -> x.points }.reversed()
            .then(FaceOffComparator(matches))
            .then(
                Comparator.comparingInt { x: PlayerStanding -> x.stats[MEDIAN_SCORE]!!.toInt() }.reversed(),
            )
            .then(
                Comparator.comparingInt { x: PlayerStanding -> x.stats[CUMULATIVE_SCORE]!!.toInt() }.reversed(),
            )
            .then(
                Comparator.comparingDouble { x: PlayerStanding -> x.stats[OPPONENT_WIN_RATE]!!.toDouble() }.reversed(),
            )

    private fun <T> MutableMap<T, Int>.incrementFor(key: T) {
        this.compute(key) { _, v -> (v ?: 0) + 1 }
    }

    inner class FaceOffComparator(
        private val matches: Collection<TournamentMatch>,
    ) : Comparator<PlayerStanding> {
        override fun compare(o1: PlayerStanding, o2: PlayerStanding): Int {
            val result =
                matches.firstOrNull { x: TournamentMatch ->
                    (x.loser == o1.name && x.winner == o2.name) ||
                        (x.loser == o2.name && x.winner == o1.name)
                }

            return if (result != null) {
                if (result.winner == o1.name) {
                    1
                } else {
                    -1
                }
            } else {
                0
            }
        }
    }
}
