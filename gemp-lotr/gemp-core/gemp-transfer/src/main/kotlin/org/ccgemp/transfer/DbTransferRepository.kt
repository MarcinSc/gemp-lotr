package org.ccgemp.transfer

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.db.DbAccessInterface
import org.sql2o.StatementRunnableWithResult

@Exposes(TransferRepository::class)
class DbTransferRepository : TransferRepository {
    @Inject
    private lateinit var dbAccess: DbAccessInterface

    override fun addTransfer(
        player: String,
        reason: String,
        notifyPlayer: Boolean,
        collectionType: String,
        direction: String,
        collection: String,
    ) {
        dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                INSERT INTO transfer (notify, player, reason, name, collection, transfer_date, direction) 
                VALUES (:notify, :player, :reason, :name, :collection, :transferDate, :direction)
                """.trimIndent()
            connection
                .createQuery(sql)
                .addParameter("notify", notifyPlayer)
                .addParameter("player", player)
                .addParameter("reason", reason)
                .addParameter("name", collectionType)
                .addParameter("collection", collectionType)
                .addParameter("transferDate", System.currentTimeMillis())
                .addParameter("direction", direction)
                .executeUpdate()
        }
    }

    override fun hasUnnotifiedTransfers(player: String): Boolean {
        return dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql = "select count(*) from transfer where player = :player and notify = 1"
                val count =
                    connection.createQuery(sql)
                        .addParameter("player", player)
                        .executeAndFetchFirst(Int::class.java)
                count > 0
            },
        )
    }

    override fun consumeUnnotifiedTransfers(player: String): Map<String, List<String>> {
        return dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val transfersSql = "SELECT name, collection FROM transfer WHERE player = :player AND notify = 1"

                val unnotifiedTransfers =
                    connection.createQuery(transfersSql)
                        .addParameter("player", player)
                        .executeAndFetch(UnnotifiedTransfer::class.java)

                val result = unnotifiedTransfers.groupBy { it.name!! }.mapValues { it.value.map { transfer -> transfer.collection!! } }

                val notifySql = "UPDATE TRANSFER SET notify = 0 WHERE player = :player AND notify = 1"
                connection.createQuery(notifySql)
                    .addParameter("player", player)
                    .executeUpdate()

                result
            },
        )
    }
}
