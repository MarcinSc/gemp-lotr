package org.ccgemp.tournament.composite.kickoff

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.JsonWithConfig
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Example configuration
 * {@code
 * {
 *      type: pause
 *      pause: 30000
 * }
 * }
 * {@code
 * {
 *      type: pauseUntil
 *      time: 2024-11-23 00:00:00
 * }
 * }
 */
@Exposes(LifecycleObserver::class)
class TimedKickoffProvider : LifecycleObserver {
    @Inject
    private lateinit var registry: TournamentKickoffRegistry

    override fun afterContextStartup() {
        val pauseProvider: (JsonWithConfig<KickoffConfig>) -> Kickoff = {
            TimedKickoff(it.json.getLong("pause", 0))
        }
        registry.register("pause", pauseProvider)

        val pauseUntilProvider: (JsonWithConfig<KickoffConfig>) -> Kickoff = {
            TimedUntilKickoff(
                LocalDateTime.parse(
                    it.json.getString("time", null),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                ),
            )
        }
        registry.register("pauseUntil", pauseUntilProvider)
    }
}

private class TimedKickoff(
    private val pause: Long,
) : Kickoff {
    private var firstInvocation: MutableMap<Int, Long> = mutableMapOf()

    override fun isKickedOff(round: Int): Boolean {
        val roundFirstInvocation = firstInvocation[round]
        if (roundFirstInvocation == null) {
            firstInvocation[round] = System.currentTimeMillis()
            return false
        } else {
            return roundFirstInvocation + pause < System.currentTimeMillis()
        }
    }
}

private class TimedUntilKickoff(
    private val until: LocalDateTime,
) : Kickoff {
    override fun isKickedOff(round: Int): Boolean {
        return !LocalDateTime.now().isBefore(until)
    }
}
