package org.ccgemp.collection

import com.gempukku.context.resolver.expose.Exposes
import org.sql2o.StatementRunnableWithResult

@Exposes(CollectionRepository::class)
class ExtendedDbCollectionRepository : AbstractDbCollectionRepository() {
    override fun addToCollection(collectionInfo: CollectionInfo, collectionChange: CollectionChange) {
        val sql =
            """
            INSERT INTO collection_entries(collection_id, product, quantity, source)
            VALUES (:collectionId, :product, :quantity, :source)
            ON DUPLICATE KEY UPDATE quantity = quantity + :quantity, source = :source;
            """.trimIndent()

        dbAccess.openDB().runInTransaction(
            StatementRunnableWithResult { connection, _ ->
                val query =
                    connection
                        .createQuery(sql)
                collectionChange.collection.all.forEach {
                    query.addParameter("collectionId", collectionInfo.id)
                        .addParameter("product", it.product)
                        .addParameter("quantity", it.count)
                        .addParameter("source", collectionChange.reason)
                        .executeUpdate()
                }
            },
        )
    }

    override fun removeFromCollection(collectionInfo: CollectionInfo, collectionChange: CollectionChange) {
        val sql =
            """
            UPDATE collection_entries SET quantity = GREATEST(quantity - :quantity, 0), source = :source
            WHERE collection_id = :collectionId and product = :product;
            """.trimIndent()

        dbAccess.openDB().runInTransaction(
            StatementRunnableWithResult { connection, _ ->
                val query =
                    connection
                        .createQuery(sql)
                collectionChange.collection.all.forEach {
                    query.addParameter("collectionId", collectionInfo.id)
                        .addParameter("product", it.product)
                        .addParameter("quantity", it.count)
                        .addParameter("source", collectionChange.reason)
                        .executeUpdate()
                }
            },
        )
    }
}
