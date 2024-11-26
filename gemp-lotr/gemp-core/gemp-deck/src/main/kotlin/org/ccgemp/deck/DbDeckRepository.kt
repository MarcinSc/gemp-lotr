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

    override fun upsertDeck(
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
                    ON DUPLICATE KEY UPDATE notes = :notes, target_format = :targetFormat, contents = :contents;
                    """.trimIndent()
                connection.createQuery(sql).addParameter("player", player).addParameter("name", name).addParameter("notes", notes).addParameter("targetFormat", targetFormat)
                    .addParameter("contents", contents).executeUpdate()
            },
        )
    }

    override fun renameDeck(player: String, oldDeckName: String, newDeckName: String): Boolean {
        return dbAccess.openDB().runInTransaction(
            StatementRunnableWithResult { connection, _ ->
                val sql =
                    """
                    UPDATE deck set name = :newDeckName
                    WHERE player = :player and name = :oldDeckName
                    """.trimIndent()
                connection
                    .createQuery(sql)
                    .addParameter("player", player)
                    .addParameter("oldDeckName", oldDeckName)
                    .addParameter("newDeckName", newDeckName)
                    .executeUpdate().result > 0
            },
        )
    }

    override fun deleteDeck(player: String, deckName: String) {
        dbAccess.openDB().runInTransaction(
            StatementRunnableWithResult { connection, _ ->
                val sql =
                    """
                    DELETE FROM deck
                    WHERE player = :player and name = :deckName
                    """.trimIndent()
                connection
                    .createQuery(sql)
                    .addParameter("player", player)
                    .addParameter("deckName", deckName)
                    .executeUpdate()
            },
        )
    }
}
