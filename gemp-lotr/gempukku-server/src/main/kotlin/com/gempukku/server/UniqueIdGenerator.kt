package com.gempukku.server

import java.util.concurrent.ThreadLocalRandom

private val UNIQUE_ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()

fun generateUniqueId(): String {
    val rnd = ThreadLocalRandom.current()
    val result = StringBuilder()
    for (i in 1..20) result.append(UNIQUE_ID_CHARS[rnd.nextInt(UNIQUE_ID_CHARS.size)])
    return result.toString()
}