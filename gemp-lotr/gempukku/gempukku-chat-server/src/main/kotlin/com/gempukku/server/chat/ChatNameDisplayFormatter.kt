package com.gempukku.server.chat

interface ChatNameDisplayFormatter {
    fun formatNameDisplay(playerId: String): String
}