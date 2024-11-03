package org.ccgemp.tournament.composite.matches.pairing

data class RoundPairing(
    val pairings: Set<Pair<String, String>>,
    val byes: Set<String>,
)
