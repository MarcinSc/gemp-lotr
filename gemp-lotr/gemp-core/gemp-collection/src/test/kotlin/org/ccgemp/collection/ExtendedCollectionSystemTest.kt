package org.ccgemp.collection

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.initializer.inject.AnnotationSystemInitializer
import com.gempukku.context.initializer.inject.property.YamlPropertyResolver
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import org.ccgemp.common.DefaultGempCollection
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

class ExtendedCollectionSystemTest {
    companion object {
        private var db: AutoCloseable? = null

        @JvmStatic
        @BeforeAll
        fun initDB() {
            db = DbTest.withDB(File("src/main/resources/db/extended"))
        }

        @JvmStatic
        @AfterAll
        fun closeDB() {
            db?.close()
        }
    }

    @Test
    fun testCollection() {
        val propertyResolver = YamlPropertyResolver("classpath:/db-config.yaml")

        val lifecycleSystem = LifecycleSystem()

        val context =
            DefaultGempukkuContext(
                null,
                AnnotationSystemResolver(
                    setOf(
                        CollectionSystem(),
                        ExtendedDbCollectionRepository(),
                        DbAccessSystem(),
                        lifecycleSystem,
                        TestProductLibrary(),
                    ),
                ),
                AnnotationSystemInitializer(propertyResolver),
            )
        context.initialize()

        lifecycleSystem.start()

        val collectionSystem = context.getSystems(CollectionInterface::class.java).first()

        // Test getting a collection that doesn't exist
        val noCollection = collectionSystem.findPlayerCollection("test", "test")
        assertNull(noCollection)

        // Test creating a new collection with addPlayerCollection
        assertTrue(
            collectionSystem.addPlayerCollection(
                "test",
                "test",
                CollectionChange(
                    false,
                    "",
                    DefaultGempCollection().also {
                        it.addItem("pack", 2)
                        it.addItem("selection", 1)
                    },
                ),
            ),
        )

        // Test reading collection
        val resultCollection = collectionSystem.findPlayerCollection("test", "test")
        assertNotNull(resultCollection)
        assertEquals(2, resultCollection!!.getItemCount("pack"))
        assertEquals(1, resultCollection!!.getItemCount("selection"))

        // Test creating adding cards to existing collection
        assertTrue(
            collectionSystem.addToPlayerCollection(
                "test",
                "test",
                CollectionChange(
                    false,
                    "",
                    DefaultGempCollection().also {
                        it.addItem("pack", 1)
                    },
                ),
            ),
        )

        // Test getting collections of all players
        val playerCollections = collectionSystem.getPlayerCollections("test")
        assertEquals(1, playerCollections.size)
        val playerCollection = playerCollections["test"]
        assertNotNull(playerCollection)
        assertEquals(3, playerCollection!!.getItemCount("pack"))
        assertEquals(1, playerCollection!!.getItemCount("selection"))

        // Validate opening pack that doesn't exist
        assertNull(collectionSystem.openPackInCollection("test", "test", "invalid", null))
        // Validate opening selection without specifying what is the selection
        assertNull(collectionSystem.openPackInCollection("test", "test", "selection", null))
        // Validate opening selection without specifying product not in selection
        assertNull(collectionSystem.openPackInCollection("test", "test", "selection", "product3"))

        // Validate opening a real pack
        val openedPack = collectionSystem.openPackInCollection("test", "test", "pack", null)
        assertNotNull(openedPack)
        assertEquals(1, openedPack!!.getItemCount("product1"))
        assertEquals(1, openedPack!!.getItemCount("product2"))

        val openPackCollection = collectionSystem.findPlayerCollection("test", "test")
        assertNotNull(openPackCollection)
        assertEquals(2, openPackCollection!!.getItemCount("pack"))
        assertEquals(1, openPackCollection!!.getItemCount("product1"))
        assertEquals(1, openPackCollection!!.getItemCount("product2"))

        // Validate opening a selection pack
        val openedSelection = collectionSystem.openPackInCollection("test", "test", "selection", "product1")
        assertNotNull(openedSelection)
        assertEquals(1, openedSelection!!.getItemCount("product1"))
        assertEquals(0, openedSelection!!.getItemCount("product2"))

        val openSelectionCollection = collectionSystem.findPlayerCollection("test", "test")
        assertNotNull(openSelectionCollection)
        assertEquals(0, openSelectionCollection!!.getItemCount("selection"))
        assertEquals(2, openSelectionCollection!!.getItemCount("product1"))
        assertEquals(1, openSelectionCollection!!.getItemCount("product2"))

        // Validate opening a pack, after all packs of the type have been opened
        assertNull(collectionSystem.openPackInCollection("test", "test", "selection", "product1"))
    }
}
