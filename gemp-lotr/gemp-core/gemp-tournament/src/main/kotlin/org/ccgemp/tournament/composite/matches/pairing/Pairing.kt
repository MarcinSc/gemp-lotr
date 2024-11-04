package org.ccgemp.tournament.composite.matches.pairing

import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentPlayer

interface Pairing {
    fun isReady(round: Int): Boolean

    fun createPairings(
        round: Int,
        players: List<TournamentPlayer>,
        matches: List<TournamentMatch>,
        pairingGroups: Map<Int, String>,
        byeGroups: Map<Int, String>,
    ): RoundPairing?
}
