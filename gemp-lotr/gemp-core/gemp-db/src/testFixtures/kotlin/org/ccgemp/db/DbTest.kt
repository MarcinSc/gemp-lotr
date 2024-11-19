package org.ccgemp.db

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import java.io.File

class DbTest {
    companion object {
        @JvmStatic
        fun withDB(vararg migrationsPaths: File): AutoCloseable {
            val configBuilder = DBConfigurationBuilder.newBuilder()
            configBuilder.port = 13579
            val db = DB.newEmbeddedDB(configBuilder.build())
            db.start()
            db.createDB("test", "root", "")

            migrationsPaths.forEach { migrationsPath ->
                val migrations = migrationsPath.listFiles()
                migrations.sortWith { o1, o2 -> o1.name.compareTo(o2.name) }

                migrations.forEach { migration ->
                    migration.inputStream().use {
                        db.source(it, "root", "", "test")
                    }
                }
            }

            return AutoCloseable { db.stop() }
        }
    }
}
