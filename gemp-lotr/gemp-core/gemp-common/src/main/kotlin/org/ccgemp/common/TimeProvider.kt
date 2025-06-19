package org.ccgemp.common

import java.time.LocalDateTime

interface TimeProvider {
    fun now(): LocalDateTime
}