package org.ccgemp.lotr.league

import com.gempukku.lotro.competitive.BestOfOneStandingsProducer
import com.gempukku.lotro.competitive.CompetitiveMatchResult
import com.gempukku.lotro.game.LotroFormat
import com.gempukku.lotro.game.formats.LotroFormatLibrary
import com.gempukku.lotro.league.LeagueParams
import com.gempukku.lotro.league.LeaguePrizes
import com.gempukku.util.JsonUtils
import org.ccgemp.collection.CollectionChange
import org.ccgemp.collection.CollectionInterface
import org.ccgemp.collection.DefaultGempCollection
import org.ccgemp.common.TimeProvider
import org.ccgemp.league.FINISHED_STAGE
import org.ccgemp.league.League
import org.ccgemp.league.LeagueHandler
import org.ccgemp.league.LeagueInfo
import org.ccgemp.league.LeagueProgress
import java.time.LocalDateTime

private const val PLAYING_GAMES = "Playing Games"
private const val AWAITING_PRIZES = "Awaiting Prizes"

class ConstructedLeagueHandler(
    private val timeProvider: TimeProvider,
    private val collectionInterface: CollectionInterface,
    private val formatLibrary: LotroFormatLibrary,
    private val leaguePrizes: LeaguePrizes,
    private val prizeCollectionType: String,
): LeagueHandler<ConstructedLeagueHandler.ConstructedLeagueData> {
    override fun initializeLeague(league: League): ConstructedLeagueHandler.ConstructedLeagueData {
        val parsedParams: LeagueParams = JsonUtils.Convert(league.parameters, LeagueParams::class.java)
            ?: throw RuntimeException("Unable to parse raw parameters for Constructed league: ${league.parameters}")
        return ConstructedLeagueData(parsedParams)
    }

    override fun progressLeague(league: LeagueInfo<ConstructedLeagueData>, leagueProgress: LeagueProgress) {
        when (league.stage) {
            "" -> leagueProgress.updateStage(PLAYING_GAMES)
            PLAYING_GAMES -> {
                if (league.data.series.last().end.isBefore(timeProvider.now())) {
                    leagueProgress.updateStage(AWAITING_PRIZES)
                }
            }
            AWAITING_PRIZES -> {
                if (league.data.series.last().end.plusDays(1).isBefore(timeProvider.now())) {
                    var maxGamesPlayed = 0
                    for (sery in league.data.series) {
                        maxGamesPlayed += sery.matches
                    }

                    val leagueStandings = BestOfOneStandingsProducer.produceStandings(
                        league.players,
                        league.matches.map { LegacyCompetitiveMatchResult(it.winner!!, it.loser!!) },
                        2,
                        1,
                        emptyMap<String, Int>()
                    )

                    for (leagueStanding in leagueStandings) {
                        val leaguePrize = leaguePrizes.getPrizeForLeague(leagueStanding.standing, leagueStandings.size, leagueStanding.gamesPlayed, maxGamesPlayed)
                        if (leaguePrize != null) {
                            val prize = DefaultGempCollection()
                            leaguePrize.all.forEach { item ->
                                prize.addItem(item.blueprintId, item.count)
                            }
                            collectionInterface.addToPlayerCollection(leagueStanding.playerName,
                                prizeCollectionType,
                                CollectionChange(true, "End of league prizes", prize)
                            )
                        }

                        val leagueTrophies = leaguePrizes.getTrophiesForLeague(leagueStanding, leagueStandings, maxGamesPlayed)
                        if (leagueTrophies != null) {
                            val prize = DefaultGempCollection()
                            leagueTrophies.all.forEach { item ->
                                prize.addItem(item.blueprintId, item.count)
                            }
                            collectionInterface.addToPlayerCollection(leagueStanding.playerName,
                                prizeCollectionType,
                                CollectionChange(true, "End of league trophies", prize))
                        }
                    }

                    leagueProgress.updateStage(FINISHED_STAGE)
                }
            }
        }
    }

    override fun unloadLeague(league: LeagueInfo<ConstructedLeagueData>) {
    }

    inner class ConstructedLeagueData(
        leagueParams: LeagueParams,
    ) {
        val series = mutableListOf<ConstructedLeagueSerieInfo>()

        init {
            var serieStart: LocalDateTime = leagueParams.start
            var count = 1
            for (serie in leagueParams.series) {
                series.add(
                    ConstructedLeagueSerieInfo(
                        "Serie $count",
                        serieStart,
                        serieStart.plusDays(serie.duration.toLong()),
                        serie.matches, formatLibrary.getFormat(serie.format),
                    ),
                )

                serieStart = serieStart.plusDays(serie.duration.toLong())
                count++
            }
        }
    }

    inner class ConstructedLeagueSerieInfo(
        val name: String,
        val start: LocalDateTime,
        val end: LocalDateTime,
        val matches: Int,
        val format: LotroFormat,
    )

    inner class LegacyCompetitiveMatchResult(
        private val winner: String,
        private val loser: String,
    ): CompetitiveMatchResult {
        override fun getLoser(): String {
            return loser
        }

        override fun getWinner(): String {
            return winner
        }
    }
}