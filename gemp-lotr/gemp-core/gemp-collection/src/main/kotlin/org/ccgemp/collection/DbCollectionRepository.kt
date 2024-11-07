package org.ccgemp.collection

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.db.DbAccessInterface
import org.sql2o.StatementRunnableWithResult

@Exposes(CollectionRepository::class)
class DbCollectionRepository : CollectionRepository {
    @Inject
    private lateinit var dbAccess: DbAccessInterface

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

    override fun addToCollection(collectionInfo: CollectionInfo, collection: CardCollection) {
        val sql =
            """
            INSERT INTO collection_entries(collection_id, product, quantity)
            VALUES (:collectionId, :product, :quantity)
            ON DUPLICATE KEY UPDATE quantity = quantity + :quantity;
            """.trimIndent()

        dbAccess.openDB().runInTransaction(
            StatementRunnableWithResult { connection, _ ->
                val query =
                    connection
                        .createQuery(sql)
                collection.all.forEach {
                    query.addParameter("collectionId", collectionInfo.id)
                        .addParameter("product", it.product)
                        .addParameter("quantity", it.count)
                        .executeUpdate()
                }
            },
        )
    }

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
}
