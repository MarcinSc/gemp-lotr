package com.gempukku.server.chat

interface IgnoreSystem {
    fun isIgnored(by: String, otherUser: String): Boolean
}