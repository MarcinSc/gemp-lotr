package org.ccgemp.deck

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.db.DbAccessInterface
import org.sql2o.StatementRunnableWithResult

@Exposes(DeckRepository::class)
class DbDeckRepository : DeckRepository {
    @Inject
    private lateinit var dbAccess: DbAccessInterface

    private val selectDeck =
        """
        SELECT
            name,
            notes,
            target_format,
            contents
        FROM deck
        """.trimIndent()

    override fun findDeck(player: String, name: String): GameDeckInfo? =
        dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String =
                    selectDeck +
                            """
                            WHERE player_id = :player and name = :name
                            """.trimIndent()
                val result: List<GameDeckInfo> =
                    connection
                        .createQuery(sql)
                        .addParameter("player", player)
                        .addParameter("name", name)
                        .executeAndFetch(GameDeckInfo::class.java)

                result.firstOrNull()
            },
        )
}