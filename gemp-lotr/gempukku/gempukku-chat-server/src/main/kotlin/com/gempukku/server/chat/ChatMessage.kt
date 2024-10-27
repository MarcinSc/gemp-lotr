package com.gempukku.server.chat

import java.util.*

data class ChatMessage(
    val date: Date,
    val from: String,
    val message: String,
    val fromAdmin: Boolean,
)