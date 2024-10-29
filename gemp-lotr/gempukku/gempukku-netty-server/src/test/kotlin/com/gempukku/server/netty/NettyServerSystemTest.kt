package com.gempukku.server.netty

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.decorator.SimpleThreadPoolFactory
import com.gempukku.context.processor.inject.decorator.WorkerThreadExecutorSystem
import com.gempukku.context.processor.inject.property.YamlPropertyResolver
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import com.gempukku.context.update.UpdatingSystem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors


class NettyServerSystemTest {
    @Test
    fun testStartupAndShutdown() {
        val threadPoolFactory = SimpleThreadPoolFactory("Worker-Thread")
        val executorService = Executors.newSingleThreadScheduledExecutor(threadPoolFactory)

        val workerThreadExecutorSystem = WorkerThreadExecutorSystem(threadPoolFactory, executorService)

        val nettyServerSystem = NettyServerSystem()
        val lifecycleSystem = LifecycleSystem()

        val propertyResolver = NettyServerSystemTest::class.java.getResourceAsStream("/netty-server-test.yaml")!!.use {
            YamlPropertyResolver(it)
        }

        val context = DefaultGempukkuContext(
            null, AnnotationSystemResolver(), AnnotationSystemInjector(propertyResolver),
            nettyServerSystem, workerThreadExecutorSystem, ExampleRequestHandler(), lifecycleSystem, UpdatingSystem()
        )

        context.initialize()

        // Start server
        lifecycleSystem.start()

        val urlTest = URL("http://localhost:8080/test")
        val conTest = urlTest.openConnection() as HttpURLConnection
        conTest.setRequestMethod("GET")
        assertEquals(404, conTest.responseCode)
        conTest.disconnect()

        val urlExample = URL("http://localhost:8080/example")
        val conExample = urlExample.openConnection() as HttpURLConnection
        conExample.setRequestMethod("GET")
        assertEquals(200, conExample.responseCode)
        val reader = BufferedReader(
            InputStreamReader(conExample.inputStream)
        )
        var inputLine: String?
        val content = StringBuffer()
        while ((reader.readLine().also { inputLine = it }) != null) {
            content.append(inputLine)
        }
        reader.close()
        conExample.disconnect()
        assertEquals("<html><body>Hello world!</body></html>", content.toString())

        // Stop server
        lifecycleSystem.stop()
    }
}