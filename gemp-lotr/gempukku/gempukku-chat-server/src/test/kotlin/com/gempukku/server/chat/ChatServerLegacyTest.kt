package com.gempukku.server.chat

import com.gempukku.context.DefaultGempukkuContext
import com.gempukku.context.lifecycle.LifecycleSystem
import com.gempukku.context.processor.inject.AnnotationSystemInjector
import com.gempukku.context.processor.inject.decorator.WorkerThreadExecutorSystem
import com.gempukku.context.processor.inject.property.YamlPropertyResolver
import com.gempukku.context.resolver.expose.AnnotationSystemResolver
import com.gempukku.context.update.UpdatingSystem
import com.gempukku.server.chat.polling.legacy.LegacyChatEventSinkProducer
import com.gempukku.server.login.LoggedUserSystem
import com.gempukku.server.netty.NettyServerSystem
import com.gempukku.server.polling.LongPollingSystem
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.util.EntityUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class ChatServerLegacyTest {
    @Test
    fun runTestScenario() {
        val lifecycleSystem = LifecycleSystem()
        val chatSystem = ChatSystem()
        val loggedUser = LoggedUserSystem()
        val workerThreadExecutorSystem = WorkerThreadExecutorSystem()
        val propertyResolver =
            YamlPropertyResolver(ChatServerLegacyTest::class.java.getResourceAsStream("/chat-server-config.yml")!!)
        val context = DefaultGempukkuContext(
            null, AnnotationSystemResolver(), AnnotationSystemInjector(propertyResolver, workerThreadExecutorSystem),
            lifecycleSystem,
            chatSystem,
            ChatApiSystem(),
            LegacyChatEventSinkProducer(),
            NettyServerSystem(),
            loggedUser,
            UpdatingSystem(), LongPollingSystem(), workerThreadExecutorSystem,
        )
        context.initialize()

        // Start server
        lifecycleSystem.start()

        val authCookie = loggedUser.logUser("player1", emptySet())
        chatSystem.createChatRoom("room", false, emptyMap(), "Welcome!")

        val cookieStore = BasicCookieStore()
        cookieStore.addCookies(arrayOf(BasicClientCookie("loggedUser", authCookie).also { it.domain = "localhost" }))
        val httpclient = HttpClients.custom()
            .setDefaultCookieStore(cookieStore)
            .build()

        val get = HttpGet("http://localhost:8080/chat/room")

        val responseHandler: ResponseHandler<Document?> =
            ResponseHandler { response ->
                val status = response.statusLine.statusCode
                if (status >= 200 && status < 300) {
                    val entity = response.entity
                    if (entity != null) {
                        val factory = DocumentBuilderFactory.newInstance()
                        val docBuilder = factory.newDocumentBuilder()
                        docBuilder.parse(ByteArrayInputStream(EntityUtils.toString(entity).toByteArray()))
                    } else {
                        null
                    }
                } else {
                    throw ClientProtocolException("Unexpected response status: $status")
                }
            }

        val getResponseBody = httpclient.execute<Document?>(get, responseHandler)
        val pollId = getResponseBody.getElementsByTagName("chat").item(0).attributes.getNamedItem("pollId").nodeValue
        validateGetResponse(getResponseBody)

        val post = HttpPost("http://localhost:8080/chat/room")
        post.entity = StringEntity("message=Outgoing+message&pollId=$pollId")

        val postResponseBody = httpclient.execute<Document?>(post, responseHandler)
        validatePostResponse(postResponseBody)

        // Stop server
        lifecycleSystem.stop()
    }

    private fun validatePostResponse(postResponseBody: Document) {
        val chatElem = postResponseBody.getElementsByTagName("chat").item(0)
        val messageElem = chatElem.childNodes.item(0)
        assertEquals("message", messageElem.nodeName)
        assertEquals("player1", messageElem.attributes.getNamedItem("from").nodeValue)
        assertEquals("Outgoing message", messageElem.childNodes.item(0).nodeValue)

        val userElem = chatElem.childNodes.item(1)
        assertEquals("user", userElem.nodeName)
        assertEquals("player1", userElem.childNodes.item(0).nodeValue)
    }

    private fun validateGetResponse(getResponseBody: Document) {
        val chatElem = getResponseBody.getElementsByTagName("chat").item(0)
        val welcomeElem = chatElem.childNodes.item(0)
        assertEquals("message", welcomeElem.nodeName)
        assertEquals("System", welcomeElem.attributes.getNamedItem("from").nodeValue)
        assertEquals("Welcome!", welcomeElem.childNodes.item(0).nodeValue)

        val userElem = chatElem.childNodes.item(1)
        assertEquals("user", userElem.nodeName)
        assertEquals("player1", userElem.childNodes.item(0).nodeValue)
    }

    private fun toString(document: Document): String {
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        val result = StreamResult(StringWriter())
        val source = DOMSource(document)
        transformer.transform(source, result)
        return result.writer.toString()
    }
}