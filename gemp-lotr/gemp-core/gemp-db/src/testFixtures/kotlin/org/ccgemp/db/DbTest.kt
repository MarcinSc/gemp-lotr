package org.ccgemp.db

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import java.io.File
import java.sql.DriverManager

class DbTest {
    companion object {
        @JvmStatic
        fun withDB(migrationsPath: File): AutoCloseable {
            val configBuilder = DBConfigurationBuilder.newBuilder()
            configBuilder.port = 13579
            val db = DB.newEmbeddedDB(configBuilder.build())
            db.start()

            Class.forName("com.mysql.cj.jdbc.Driver")

            val migrations = migrationsPath.listFiles()
            migrations.sortWith { o1, o2 -> o1.name.compareTo(o2.name) }
            val connection = DriverManager.getConnection("jdbc:mysql://localhost:13579/test", "root", "")
            connection.use {
                val statement = connection.createStatement()
                statement.use {
                    migrations.forEach { migration ->
                        val sql = migration.readText()
                        sql.split(";").filter { it.trim().isNotEmpty() }.forEach {
                            statement.execute(it)
                        }
                    }
                }
            }

            return AutoCloseable { db.stop() }
        }
    }
}
