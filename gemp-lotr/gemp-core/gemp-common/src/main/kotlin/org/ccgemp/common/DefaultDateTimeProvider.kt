package org.ccgemp.common

import com.gempukku.context.resolver.expose.Exposes
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC

@Exposes(TimeProvider::class)
class DefaultDateTimeProvider: TimeProvider {
    override fun now(): LocalDateTime {
        return LocalDateTime.now(UTC)
    }
}

fun LocalDateTime.toEpochMilli(): Long {
    return this.toInstant(UTC).toEpochMilli()
}