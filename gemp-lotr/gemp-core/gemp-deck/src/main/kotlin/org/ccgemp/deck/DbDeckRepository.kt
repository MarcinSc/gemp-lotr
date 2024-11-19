package org.ccgemp.deck

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.db.DbAccessInterface
import org.sql2o.StatementRunnableWithResult

@Exposes(DeckRepository::class)
class DbDeckRepository : DeckRepository {
    @Inject
    private lateinit var dbAccess: DbAccessInterface

    private val selectDeck = """
        SELECT
            name,
            notes,
            target_format,
            contents
        FROM deck
        """

    override fun findDeck(player: String, name: String): GameDeckInfo? =
        dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String =
                    selectDeck +
                        """
                        WHERE player = :player and name = :name
                        """.trimIndent()
                val result =
                    connection
                        .createQuery(sql)
                        .addParameter("player", player)
                        .addParameter("name", name)
                        .executeAndFetch(GameDeckInfo::class.java)

                result.firstOrNull()
            },
        )

    override fun getPlayerDecks(player: String): List<GameDeckInfo> =
        dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String =
                    selectDeck +
                        """
                        WHERE player = :player
                        """.trimIndent()
                connection
                    .createQuery(sql)
                    .addParameter("player", player)
                    .executeAndFetch(GameDeckInfo::class.java)
            },
        )

    override fun createDeck(
        player: String,
        name: String,
        notes: String,
        targetFormat: String,
        contents: String,
    ) {
        dbAccess.openDB().runInTransaction(
            StatementRunnableWithResult { connection, _ ->
                val sql =
                    """
                    INSERT deck (player, name, notes, target_format, contents)
                    VALUES (:player, :name, :notes, :targetFormat, :contents)
                    """.trimIndent()
                connection.createQuery(sql).addParameter("player", player).addParameter("name", name).addParameter("notes", notes).addParameter("targetFormat", targetFormat)
                    .addParameter("contents", contents).executeUpdate()
            },
        )
    }
}
