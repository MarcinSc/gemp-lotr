package com.gempukku.context.decorator

interface ThreadPool {
    fun containsThread(thread: Thread): Boolean
}
