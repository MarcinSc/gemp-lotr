package com.gempukku.context.processor.inject.decorator

interface ThreadPool {
    fun containsThread(thread: Thread): Boolean
}
