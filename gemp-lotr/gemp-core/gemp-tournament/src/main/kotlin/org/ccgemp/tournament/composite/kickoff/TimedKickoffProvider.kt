package org.ccgemp.tournament.composite.kickoff

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.common.TimeProvider
import org.ccgemp.json.JsonWithConfig
import java.time.Duration
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
    private lateinit var timeProvider: TimeProvider

    @Inject
    private lateinit var registry: TournamentKickoffRegistry

    override fun afterContextStartup() {
        val pauseProvider: (JsonWithConfig<KickoffConfig>) -> Kickoff = {
            TimedKickoff(timeProvider, Duration.ofMillis(it.json.getLong("pause", 0)))
        }
        registry.register("pause", pauseProvider)

        val pauseUntilProvider: (JsonWithConfig<KickoffConfig>) -> Kickoff = {
            TimedUntilKickoff(
                timeProvider,
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
    private val timeProvider: TimeProvider,
    private val pause: Duration,
) : Kickoff {
    private var firstInvocation: MutableMap<Int, LocalDateTime> = mutableMapOf()

    override fun isKickedOff(round: Int): Boolean {
        val roundFirstInvocation = firstInvocation[round]
        if (roundFirstInvocation == null) {
            firstInvocation[round] = timeProvider.now()
            return false
        } else {
            return roundFirstInvocation.plus(pause).isBefore(timeProvider.now())
        }
    }
}

private class TimedUntilKickoff(
    private val timeProvider: TimeProvider,
    private val until: LocalDateTime,
) : Kickoff {
    override fun isKickedOff(round: Int): Boolean {
        return !timeProvider.now().isBefore(until)
    }
}
