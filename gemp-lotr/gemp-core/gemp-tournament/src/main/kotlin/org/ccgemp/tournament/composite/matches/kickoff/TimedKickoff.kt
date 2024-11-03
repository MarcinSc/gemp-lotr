package org.ccgemp.tournament.composite.matches.kickoff

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import org.hjson.JsonObject

class TimedKickoff : LifecycleObserver {
    @Inject
    private lateinit var registry: TournamentKickoffRegistry

    override fun afterContextStartup() {
        val kickoffProvider: (JsonObject) -> Kickoff = {
            TimerKickoff(it.getLong("pause", 0))
        }
        registry.register(
            "timed",
            kickoffProvider,
        )
    }
}

class TimerKickoff(
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
