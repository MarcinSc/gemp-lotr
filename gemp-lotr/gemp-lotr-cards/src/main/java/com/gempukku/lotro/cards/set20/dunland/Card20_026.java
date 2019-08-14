package com.gempukku.lotro.cards.set20.dunland;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * 1
 * Savage Tactics
 * Dunland	Event • Maneuver
 * Spot two [Dunland] Men to wound an unbound archer companion. If you control a site, wound that companion again.
 */
public class Card20_026 extends AbstractEvent {
    public Card20_026() {
        super(Side.SHADOW, 1, Culture.DUNLAND, "Savage Tactics", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, 2, Culture.DUNLAND, Race.MAN);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, PlayConditions.controllsSite(game, playerId) ? 2 : 1, Filters.unboundCompanion, Keyword.ARCHER));
        return action;
    }
}
