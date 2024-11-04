package org.ccgemp.tournament.composite.matches.pairing

import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.TypedRegistry

@Exposes(TournamentPairingRegistry::class)
class TournamentPairingSystem : TournamentPairingRegistry, TypedRegistry<PairingConfig, Pairing>()
