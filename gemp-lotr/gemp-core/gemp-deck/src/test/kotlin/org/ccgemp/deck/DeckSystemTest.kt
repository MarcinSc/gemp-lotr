package org.ccgemp.deck

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.initializer.inject.AnnotationSystemInitializer
import com.gempukku.context.initializer.inject.property.YamlPropertyResolver
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.ccgemp.common.GameDeck
import org.ccgemp.common.GameDeckItem
import org.ccgemp.db.DbAccessSystem
import org.ccgemp.db.DbTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
                        SimpleDbDeckSerialization(),
                        DbAccessSystem(),
                        lifecycleSystem,
                    ),
                ),
                AnnotationSystemInitializer(propertyResolver),
            )
        context.initialize()

        lifecycleSystem.start()

        val deckSystem = context.getSystems(DeckInterface::class.java).first()

        val noDeck = deckSystem.findDeck("test", "Name")
        assertNull(noDeck)

        val noPlayerDecks = deckSystem.getPlayerDecks("test")
        assertEquals(0, noPlayerDecks.size)

        val newDeck =
            GameDeck(
                "Name",
                "Notes",
                "format",
                mapOf("ring" to listOf(GameDeckItem("1", 1), GameDeckItem("2", 1)), "ringBearer" to listOf(GameDeckItem("3,4", 1), GameDeckItem("5", 1))),
            )
        deckSystem.saveDeck("test", newDeck)

        val resultDeck = deckSystem.findDeck("test", "Name")
        assertNotNull(resultDeck)
        assertEquals("Name", resultDeck!!.name)
        assertEquals("Notes", resultDeck.notes)
        assertEquals("format", resultDeck.targetFormat)
        val deckParts = resultDeck.deckParts
        assertEquals(2, deckParts.size)
        assertEquals(listOf(GameDeckItem("1", 1), GameDeckItem("2", 1)), deckParts["ring"])
        assertEquals(listOf(GameDeckItem("3,4", 1), GameDeckItem("5", 1)), deckParts["ringBearer"])

        val playerDecks = deckSystem.getPlayerDecks("test")
        assertEquals(1, playerDecks.size)
    }
}
