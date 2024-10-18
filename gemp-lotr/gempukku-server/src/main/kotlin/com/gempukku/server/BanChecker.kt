package com.gempukku.server

interface BanChecker {
    fun isBanned(ipString: String): Boolean
}