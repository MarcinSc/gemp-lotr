package org.ccgemp.tournament.composite.kickoff

import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.json.TypedRegistry

@Exposes(TournamentKickoffRegistry::class)
class TournamentKickoffSystem : TournamentKickoffRegistry, TypedRegistry<KickoffConfig, Kickoff>()
