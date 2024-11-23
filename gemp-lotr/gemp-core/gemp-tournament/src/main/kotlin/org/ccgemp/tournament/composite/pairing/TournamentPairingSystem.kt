package org.ccgemp.tournament.composite.pairing

import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.TypedRegistry

@Exposes(TournamentPairingRegistry::class)
class TournamentPairingSystem : TournamentPairingRegistry, TypedRegistry<PairingConfig, Pairing>()
