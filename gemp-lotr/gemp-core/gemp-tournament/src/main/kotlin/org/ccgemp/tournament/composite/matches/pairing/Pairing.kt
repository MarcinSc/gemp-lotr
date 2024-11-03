package org.ccgemp.tournament.composite.matches.pairing

import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentPlayer

interface Pairing {
    fun createPairings(
        players: List<TournamentPlayer>,
        matches: List<TournamentMatch>,
        pairingGroups: Map<Int, String>,
        byeGroups: Map<Int, String>,
    ): RoundPairing
}
