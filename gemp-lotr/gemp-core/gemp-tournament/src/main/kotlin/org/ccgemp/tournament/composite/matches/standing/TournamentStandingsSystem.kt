package org.ccgemp.tournament.composite.matches.standing

import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.TypedRegistry

@Exposes(TournamentStandingsRegistry::class)
class TournamentStandingsSystem : TournamentStandingsRegistry, TypedRegistry<StandingsConfig, Standings>()
