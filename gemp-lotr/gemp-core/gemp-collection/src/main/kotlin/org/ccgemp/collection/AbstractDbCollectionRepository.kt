package org.ccgemp.collection

import com.gempukku.context.initializer.inject.Inject
import org.ccgemp.db.DbAccessInterface
import org.sql2o.StatementRunnableWithResult

abstract class AbstractDbCollectionRepository : CollectionRepository {
    @Inject
    protected lateinit var dbAccess: DbAccessInterface

    private val selectCollection =
        """
        SELECT
            id,
            player,
            type
        FROM collection
        """

    private val selectCollectionEntry =
        """
        SELECT
            collection_id,
            product,
            quantity
        FROM collection_entries
        """

    override fun findPlayerCollection(player: String, type: String): CollectionInfo? =
        dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql =
                    selectCollection +
                        """
                        WHERE player = :player and type = :type
                        """.trimIndent()
                val result =
                    connection
                        .createQuery(sql)
                        .addParameter("player", player)
                        .addParameter("type", type)
                        .executeAndFetch(CollectionInfo::class.java)

                result.firstOrNull()
            },
        )

    override fun getPlayerCollectionEntries(collection: Set<CollectionInfo>): List<CollectionEntryInfo> =
        dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql =
                    selectCollectionEntry +
                        """
                        WHERE collection_id in (:collectionIds)    
                        """.trimIndent()
                connection.createQuery(sql)
                    .addParameter("collectionIds", collection.map { it.id })
                    .executeAndFetch(CollectionEntryInfo::class.java)
            },
        )

    override fun createCollection(player: String, type: String): CollectionInfo =
        dbAccess.openDB().runInTransaction(
            StatementRunnableWithResult { connection, _ ->
                val sql =
                    """
                    INSERT INTO collection (player, type)
                    VALUES (:player, :type)
                    """.trimIndent()
                connection
                    .createQuery(sql, true)
                    .addParameter("player", player)
                    .addParameter("type", type)
                    .executeUpdate()
                CollectionInfo(connection.getKeys(Int::class.java).first(), player, type)
            },
        )

    override fun findCollectionsByType(type: String): List<CollectionInfo> =
        dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String =
                    selectCollection +
                        """
                        WHERE type = :type
                        """.trimIndent()
                connection
                    .createQuery(sql)
                    .addParameter("type", type)
                    .executeAndFetch(CollectionInfo::class.java)
            },
        )

    override fun getItemCount(player: String, type: String, product: String): Int {
        return dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql =
                    """
                    SELECT quantity from collection_entries ce join collection c on ce.collection_id = c.id
                    WHERE c.player= :player and c.type = :type and ce.product = :product
                    """.trimIndent()
                val result =
                    connection
                        .createQuery(sql)
                        .addParameter("player", player)
                        .addParameter("type", type)
                        .addParameter("product", product)
                        .executeAndFetch(Int::class.java)

                result.firstOrNull() ?: 0
            },
        )
    }
}
