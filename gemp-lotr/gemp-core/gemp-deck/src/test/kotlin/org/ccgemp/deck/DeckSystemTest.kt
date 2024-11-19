package org.ccgemp.deck

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.property.YamlPropertyResolver
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.ccgemp.db.DbAccessSystem
import org.ccgemp.db.DbTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

class DeckSystemTest {
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
    fun testDeck() {
        val propertyResolver = YamlPropertyResolver("classpath:/db-config.yaml")

        val lifecycleSystem = LifecycleSystem()

        val context =
            DefaultGempukkuContext(
                null,
                AnnotationSystemResolver(
                    setOf(
                        DeckSystem(),
                        DbDeckRepository(),
                        SimpleDeckSerialization(),
                        NoopDeckValidation(),
                        DbAccessSystem(),
                        lifecycleSystem,
                    ),
                ),
                AnnotationSystemInjector(propertyResolver),
            )
        context.initialize()

        lifecycleSystem.start()

        val deckSystem = context.getSystems(DeckInterface::class.java).first()

        val noDeck = deckSystem.findDeck("test", "Name")
        assertNull(noDeck)

        val noPlayerDecks = deckSystem.getPlayerDecks("test")
        assertEquals(0, noPlayerDecks.size)

        val newDeck = GameDeck("Name", "Notes", "format", mapOf("ring" to listOf("1", "2"), "ringBearer" to listOf("3,4", "5")))
        assertTrue(deckSystem.addDeck("test", newDeck))

        val resultDeck = deckSystem.findDeck("test", "Name")
        assertNotNull(resultDeck)
        assertEquals("Name", resultDeck!!.name)
        assertEquals("Notes", resultDeck.notes)
        assertEquals("format", resultDeck.targetFormat)
        val deckParts = resultDeck.deckParts
        assertEquals(2, deckParts.size)
        assertEquals(listOf("1", "2"), deckParts["ring"])
        assertEquals(listOf("3,4", "5"), deckParts["ringBearer"])

        val playerDecks = deckSystem.getPlayerDecks("test")
        assertEquals(1, playerDecks.size)
    }
}
