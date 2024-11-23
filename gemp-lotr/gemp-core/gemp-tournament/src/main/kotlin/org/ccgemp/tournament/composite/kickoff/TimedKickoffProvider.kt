package org.ccgemp.tournament.composite.kickoff

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.JsonWithConfig

/**
 * Example configuration
 * {@code
 * {
 *      type: timed
 *      pause: 30000
 * }
 * }
 */
@Exposes(LifecycleObserver::class)
class TimedKickoffProvider : LifecycleObserver {
    @Inject
    private lateinit var registry: TournamentKickoffRegistry

    override fun afterContextStartup() {
        val kickoffProvider: (JsonWithConfig<KickoffConfig>) -> Kickoff = {
            TimedKickoff(it.json.getLong("pause", 0))
        }
        registry.register("timed", kickoffProvider)
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
