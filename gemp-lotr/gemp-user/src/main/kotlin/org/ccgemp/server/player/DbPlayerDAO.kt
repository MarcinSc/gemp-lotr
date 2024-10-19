package org.ccgemp.server.player

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.db.DbAccessInterface
import org.sql2o.StatementRunnableWithResult

@Exposes(PlayerDAO::class)
class DbPlayerDAO : PlayerDAO {
    @Inject
    private lateinit var dbAccess: DbAccessInterface

    private val _selectPlayer =
        """
            SELECT 
                id, 
                name, 
                password, 
                email,
                type, 
                last_login_reward, 
                banned_until, 
                create_ip, 
                last_ip 
            FROM player
        """.trimIndent()


    override fun registerPlayer(
        login: String,
        password: String,
        email: String,
        type: String,
        remoteIp: String
    ): Boolean {
        return dbAccess.openDB().runInTransaction(
            StatementRunnableWithResult { connection, _ ->
                val sql =
                    """
                        INSERT INTO player (name, password, email, type, create_ip)
                        VALUES (:login, :password, :email, :type, :create_ip)
                    """.trimIndent()
                connection.createQuery(sql)
                    .addParameter("login", login)
                    .addParameter("password", password)
                    .addParameter("type", type)
                    .addParameter("create_ip", remoteIp)
                    .executeUpdate()

                connection.result == 1
            })
    }

    override fun loginPlayer(login: String, password: String): Player? {
        return dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String = _selectPlayer +
                        """
                            WHERE name = :login
                                AND password = :password
                        """.trimIndent()
                val result: List<Player> = connection.createQuery(sql)
                    .addParameter("login", login)
                    .addParameter("password", password)
                    .executeAndFetch(Player::class.java)

                result.firstOrNull()
            })
    }

    override fun updateLastIp(player: Player, lastIp: String) {
        return dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                    UPDATE player set last_ip = :lastIp where id = :id
                """.trimIndent()
            connection.createQuery(sql)
                .addParameter("lastIp", lastIp)
                .addParameter("id", player.id)
                .executeUpdate()
        }
    }

    override fun updateForPasswordReset(player: Player, resetToken: String) {
        return dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                    UPDATE player set password_reset_token = :resetToken where id = :id
                """.trimIndent()
            connection.createQuery(sql)
                .addParameter("resetToken", resetToken)
                .addParameter("id", player.id)
                .executeUpdate()
        }
    }

    override fun findPlayerByPasswordResetToken(resetToken: String): Player? {
        return dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String = _selectPlayer +
                        """
                            WHERE password_reset_token = :resetToken
                        """.trimIndent()
                val result: List<Player> = connection.createQuery(sql)
                    .addParameter("resetToken", resetToken)
                    .executeAndFetch(Player::class.java)

                result.firstOrNull()
            })
    }

    override fun setPassword(player: Player, password: String) {
        return dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                    UPDATE player set password = :password, password_reset_token = null where id = :id
                """.trimIndent()
            connection.createQuery(sql)
                .addParameter("password", password)
                .addParameter("id", player.id)
                .executeUpdate()
        }
    }

    override fun findPlayerByLogin(login: String): Player? {
        return dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String = _selectPlayer +
                        """
                            WHERE name = :name
                        """.trimIndent()
                val result: List<Player> = connection.createQuery(sql)
                    .addParameter("name", login)
                    .executeAndFetch(Player::class.java)

                result.firstOrNull()
            })
    }

    override fun findPlayerByEmail(email: String): Player? {
        return dbAccess.openDB().withConnection(
            StatementRunnableWithResult { connection, _ ->
                val sql: String = _selectPlayer +
                        """
                            WHERE lower(email) = :email
                        """.trimIndent()
                val result: List<Player> = connection.createQuery(sql)
                    .addParameter("email", email.lowercase())
                    .executeAndFetch(Player::class.java)

                result.firstOrNull()
            })
    }

    override fun updateForEmailChange(player: Player, newEmail: String, changeEmailToken: String) {
        return dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                    UPDATE player set new_email = :newEmail, change_email_token = :changeEmailToken where id = :id
                """.trimIndent()
            connection.createQuery(sql)
                .addParameter("newEmail", newEmail)
                .addParameter("changeEmailToken", changeEmailToken)
                .addParameter("id", player.id)
                .executeUpdate()
        }
    }

    override fun emailUpdateValidated(changeEmailToken: String) {
        return dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                    UPDATE player set email = new_email, new_email = null, change_email_token = null where change_email_token = :changeEmailToken
                """.trimIndent()
            connection.createQuery(sql)
                .addParameter("changeEmailToken", changeEmailToken)
                .executeUpdate()
        }
    }

    override fun banPlayer(player: Player) {
        return dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                    UPDATE player set type = '', banned_until = null where id = :id
                """.trimIndent()
            connection.createQuery(sql)
                .addParameter("id", player.id)
                .executeUpdate()
        }
    }

    override fun banPlayers(players: List<Player>) {
        return dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                    UPDATE player set type = '', banned_until = null where id in :ids
                """.trimIndent()
            connection.createQuery(sql)
                .addParameter("ids", players.map { it.id })
                .executeUpdate()
        }
    }

    override fun banPlayerTemporarily(player: Player, bannedUntil: Long) {
        return dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                    UPDATE player set type = 'un', banned_until = :bannedUntil where id = :id
                """.trimIndent()
            connection.createQuery(sql)
                .addParameter("bannedUntil", bannedUntil)
                .addParameter("id", player.id)
                .executeUpdate()
        }
    }

    override fun unbanPlayer(player: Player) {
        return dbAccess.openDB().runInTransaction { connection, _ ->
            val sql =
                """
                    UPDATE player set type = 'un', banned_until = null where id = :id
                """.trimIndent()
            connection.createQuery(sql)
                .addParameter("id", player.id)
                .executeUpdate()
        }
    }
}