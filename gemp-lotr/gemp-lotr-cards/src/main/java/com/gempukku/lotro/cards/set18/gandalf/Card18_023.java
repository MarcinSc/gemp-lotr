package com.gempukku.lotro.cards.set18.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.RemoveBurdenEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 2
 * Type: Event • Maneuver
 * Game Text: Spell. Spot 2 pipeweed cards to remove a burden for each [GANDALF] Wizard you can spot.
 */
public class Card18_023 extends AbstractEvent {
    public Card18_023() {
        super(Side.FREE_PEOPLE, 2, Culture.GANDALF, "One-Upsmanship", Phase.MANEUVER);
        addKeyword(Keyword.SPELL);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, 2, Keyword.PIPEWEED);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        int count = Filters.countActive(game, Culture.GANDALF, Race.WIZARD);
        if (count > 0)
            action.appendEffect(
                    new RemoveBurdenEffect(playerId, self, count));
        return action;
    }
}
