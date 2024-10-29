package com.gempukku.context.processor.inject.decorator

import java.util.Collections
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class SimpleThreadPoolFactory(
    private val namePrefix: String,
) : ThreadFactory,
    ThreadPool {
    private val threadNumber = AtomicInteger(1)
    private val threadPool = Collections.synchronizedSet(mutableSetOf<Thread>())

    override fun newThread(r: Runnable): Thread {
        val t = Thread(r, namePrefix + threadNumber.getAndIncrement())
        if (t.isDaemon) {
            t.setDaemon(false)
        }
        if (t.priority != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY)
        }
        return t.also {
            threadPool.add(t)
        }
    }

    override fun containsThread(thread: Thread): Boolean = threadPool.contains(thread)
}
