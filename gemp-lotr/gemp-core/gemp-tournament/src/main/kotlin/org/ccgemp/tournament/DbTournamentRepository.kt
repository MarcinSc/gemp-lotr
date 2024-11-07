package org.ccgemp.tournament

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.db.DbAccessInterface
import org.sql2o.StatementRunnableWithResult
import java.time.LocalDateTime

@Exposes(TournamentRepository::class)
class DbTournamentRepository : TournamentRepository {
    @Inject
    private lateinit var dbAccess: DbAccessInterface

    private val selectTournament =
        """
        SELECT 
            tournament_id, name, start_date, type, parameters, stage, round
        FROM tournament 
        """

    private val selectTournamentPlayer =
        """
        SELECT
            player, deck, dropped
        FROM tournament_player
        """

    private val selectTournamentMatch =
        """
        SELECT
            round, player_one, player_two, winner
        FROM tournament_match
        """

    private val selectTournamentDeck =
        """
        SELECT
            tournament_id, player, type, name, notes, target_format, contents
        FROM tournament_deck
        """

    override fun getUnfinishedOrStartAfter(time: LocalDateTime): List<Tournament> =
        dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String =
                    selectTournament +
                        """
                        WHERE stage <> :stage or start_date > :time
                        """.trimIndent()
                connection
                    .createQuery(sql)
                    .addParameter("stage", FINISHED_STAGE)
                    .addParameter("time", time)
                    .executeAndFetch(Tournament::class.java)
            },
        )

    override fun getTournamentMatches(tournamentId: String): List<TournamentMatch> =
        dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String =
                    selectTournamentMatch +
                        """
                        WHERE tournament_id = :tournamentId
                        """.trimIndent()
                connection
                    .createQuery(sql)
                    .addParameter("tournamentId", tournamentId)
                    .executeAndFetch(TournamentMatch::class.java)
            },
        )

    override fun getTournamentPlayers(tournamentId: String): List<TournamentPlayer> =
        dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String =
                    selectTournamentPlayer +
                        """
                        WHERE tournament_id = :tournamentId
                        """.trimIndent()
                connection
                    .createQuery(sql)
                    .addParameter("tournamentId", tournamentId)
                    .executeAndFetch(TournamentPlayer::class.java)
            },
        )

    override fun getTournamentDecks(tournamentId: String): List<TournamentDeck> =
        dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String =
                    selectTournamentDeck +
                        """
                        WHERE tournament_id = :tournamentId
                        """.trimIndent()
                connection
                    .createQuery(sql)
                    .addParameter("tournamentId", tournamentId)
                    .executeAndFetch(TournamentDeck::class.java)
            },
        )

    override fun setRoundAndStage(tournamentId: String, round: Int, stage: String) =
        dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                UPDATE tournament set round = :round and stage = :stage where tournament_id = :tournamentId
                """.trimIndent()
            connection
                .createQuery(sql)
                .addParameter("round", round)
                .addParameter("stage", stage)
                .addParameter("tournamentId", tournamentId)
                .executeUpdate()
        }

    override fun createMatch(
        tournamentId: String,
        round: Int,
        playerOne: String,
        playerTwo: String,
        winner: String?,
    ) = dbAccess.openDB().runInTransaction { connection, _ ->
        val sql =
            """
            INSERT INTO tournament_match (tournament_id, round, player_one, player_two, winner) 
            VALUES (:tournamentId, :round, :playerOne, :playerTwo, :winner)
            """.trimIndent()
        connection
            .createQuery(sql)
            .addParameter("tournamentId", tournamentId)
            .addParameter("round", round)
            .addParameter("playerOne", playerOne)
            .addParameter("playerTwo", playerTwo)
            .addParameter("winner", winner)
            .executeUpdate()
    }

    override fun addPlayer(tournamentId: String, player: String) =
        dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                INSERT INTO tournament_player (tournament_id, player, dropped) 
                VALUES (:tournamentId, :player, :dropped)
                """.trimIndent()
            connection
                .createQuery(sql)
                .addParameter("tournamentId", tournamentId)
                .addParameter("player", player)
                .addParameter("dropped", false)
                .executeUpdate()
        }

    override fun dropPlayer(tournamentId: String, player: String) =
        dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                UPDATE tournament_player set dropped = true
                WHERE tournament_id = :tournamentId and player = :player
                """.trimIndent()
            connection
                .createQuery(sql)
                .addParameter("tournamentId", tournamentId)
                .addParameter("player", player)
                .executeUpdate()
        }

    override fun upsertDeck(
        tournamentId: String,
        player: String,
        type: String,
        name: String,
        notes: String,
        targetFormat: String,
        contents: String,
    ) {
        dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                INSERT INTO tournament_deck(tournament_id, player, type, name, notes, target_format, contents)
                VALUES (:tournamentId, :player, :type, :name, :notes, :targetFormat, :contents)
                ON DUPLICATE KEY UPDATE name = :name, notes = :notes, target_format = :targetFormat, contents = :contents;
                """.trimIndent()
            connection
                .createQuery(sql)
                .addParameter("tournamentId", tournamentId)
                .addParameter("player", player)
                .addParameter("type", type)
                .addParameter("name", name)
                .addParameter("notes", notes)
                .addParameter("targetFormat", targetFormat)
                .addParameter("contents", contents)
                .executeUpdate()
        }
    }
}
