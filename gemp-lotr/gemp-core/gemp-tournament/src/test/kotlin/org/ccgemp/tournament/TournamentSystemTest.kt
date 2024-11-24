package org.ccgemp.tournament

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.initializer.inject.AnnotationSystemInitializer
import com.gempukku.context.initializer.inject.property.YamlPropertyResolver
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.ccgemp.db.DbAccessSystem
import org.ccgemp.db.DbTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

class TournamentSystemTest {
    companion object {
        private var db: AutoCloseable? = null

        @JvmStatic
        @BeforeAll
        fun initDB() {
            db = DbTest.withDB(File("src/main/resources/db"))
        }

        @JvmStatic
        @AfterAll
        fun closeDB() {
            db?.close()
        }
    }

    @Test
    fun testTournament() {
        val propertyResolver = YamlPropertyResolver("classpath:/db-config.yaml")

        val lifecycleSystem = LifecycleSystem()

        val context =
            DefaultGempukkuContext(
                null,
                AnnotationSystemResolver(
                    setOf(
                        TournamentSystem(),
                        DbTournamentRepository(),
                        DummyDeckSystem(),
                        DummyGameContainer<Any, Any>(),
                        DbAccessSystem(),
                        lifecycleSystem,
                    ),
                ),
                AnnotationSystemInitializer(propertyResolver),
            )
        context.initialize()

        lifecycleSystem.start()

        val tournamentSystem = context.getSystems(TournamentInterface::class.java).first()
    }
}
