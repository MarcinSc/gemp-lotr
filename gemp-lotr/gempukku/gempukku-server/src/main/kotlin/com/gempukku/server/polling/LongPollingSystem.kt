package com.gempukku.server.polling

import com.gempukku.context.processor.inject.InjectValue
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.server.generateUniqueId

@Exposes(LongPolling::class, UpdatedSystem::class)
class LongPollingSystem :
    LongPolling,
    UpdatedSystem {
    @InjectValue("server.polling.timeout")
    private var pollTimeout: Long = 10000

    @InjectValue("server.polling.channelTimeout")
    private var channelTimeout: Long = 60000

    private val pollMap: MutableMap<String, PollRegistration<*>> = mutableMapOf()

    override fun <Event> registerLongPoll(eventStream: EventStream<Event>, timeoutRunnable: Runnable?): String {
        var pollId: String
        do {
            pollId = generateUniqueId()
        } while (pollMap.containsKey(pollId))
        pollMap[pollId] = PollRegistration(System.currentTimeMillis(), eventStream, null, timeoutRunnable)
        return pollId
    }

    override fun <Event> registerSink(pollId: String, eventSink: EventSink<Event>): Boolean =
        pollMap[pollId]?.let { registration ->
            val reg = registration as PollRegistration<Event>
            reg.eventSink?.processEventsAndClose(emptyList())
            reg.lastAccessed = System.currentTimeMillis()
            reg.eventSink = eventSink
            true
        } ?: false

    override fun update() {
        val updateTime = System.currentTimeMillis()
        // Send awaiting events
        pollMap.forEach {
            val registration = it.value as PollRegistration<Any>
            registration.eventSink?.let { sink ->
                val events = registration.eventStream.consumeEvents()
                if (events.isNotEmpty() || registration.lastAccessed + pollTimeout < updateTime) {
                    sink.processEventsAndClose(events)
                    registration.eventSink = null
                }
            }
        }
        // Timeout inactive polls
        pollMap.entries.removeAll {
            val remove = it.value.lastAccessed + channelTimeout < updateTime || it.value.eventStream.isFinished()
            if (remove) {
                it.value.eventSink?.processEventsAndClose(emptyList())
                it.value.timeOutRunnable?.run()
            }
            remove
        }
    }
}

private data class PollRegistration<Event>(
    var lastAccessed: Long,
    val eventStream: EventStream<Event>,
    var eventSink: EventSink<Event>?,
    val timeOutRunnable: Runnable?,
)
