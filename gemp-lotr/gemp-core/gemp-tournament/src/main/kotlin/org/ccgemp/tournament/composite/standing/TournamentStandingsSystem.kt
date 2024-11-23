package org.ccgemp.tournament.composite.standing

import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.TypedRegistry

@Exposes(TournamentStandingsRegistry::class)
class TournamentStandingsSystem : TournamentStandingsRegistry, TypedRegistry<StandingsConfig, Standings>()
