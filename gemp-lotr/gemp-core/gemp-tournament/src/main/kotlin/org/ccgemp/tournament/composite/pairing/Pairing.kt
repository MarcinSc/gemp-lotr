package org.ccgemp.tournament.composite.pairing

import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant

interface Pairing {
    fun isReady(round: Int): Boolean

    fun createPairings(
        round: Int,
        players: List<TournamentParticipant>,
        matches: List<TournamentMatch>,
        pairingGroups: Map<Int, String>,
        byeGroups: Map<Int, String>,
    ): RoundPairing?
}
