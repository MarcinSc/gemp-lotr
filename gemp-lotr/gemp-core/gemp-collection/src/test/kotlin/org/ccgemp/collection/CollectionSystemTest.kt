package org.ccgemp.collection

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

class CollectionSystemTest {
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
    fun testCollection() {
        val propertyResolver = YamlPropertyResolver("classpath:/db-config.yaml")

        val lifecycleSystem = LifecycleSystem()

        val context =
            DefaultGempukkuContext(
                null,
                AnnotationSystemResolver(),
                AnnotationSystemInjector(propertyResolver),
                setOf(
                    CollectionSystem(),
                    DbCollectionRepository(),
                    DbAccessSystem(),
                    lifecycleSystem,
                ),
            )
        context.initialize()

        lifecycleSystem.start()

        val collectionSystem = context.getSystems(CollectionInterface::class.java).first()

        val noCollection = collectionSystem.findPlayerCollection("test", "test")
        assertNull(noCollection)

        assertTrue(
            collectionSystem.addPlayerCollection(
                "test",
                "test",
                false,
                "",
                DefaultCardCollection().also {
                    it.addItem("product", 2)
                },
            ),
        )

        val resultCollection = collectionSystem.findPlayerCollection("test", "test")
        assertNotNull(resultCollection)
        assertEquals(2, resultCollection!!.getItemCount("product"))

        assertTrue(
            collectionSystem.addToPlayerCollection(
                "test",
                "test",
                false,
                "",
                DefaultCardCollection().also {
                    it.addItem("product", 1)
                },
            ),
        )

        val playerCollections = collectionSystem.getPlayerCollections("test")
        assertEquals(1, playerCollections.size)
        val playerCollection = playerCollections["test"]
        assertNotNull(playerCollection)
        assertEquals(3, playerCollection!!.getItemCount("product"))
    }
}
