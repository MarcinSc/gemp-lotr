package com.gempukku.lotro.cards.set20.dunland;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndStackCardsFromDiscardEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * 0
 * Running Rampant
 * Dunland	Event • Maneuver
 * Discard a [Dunland] card from hand to stack a [Dunland] Man from your discard pile on a site you control.
 */
public class Card20_027 extends AbstractEvent {
    public Card20_027() {
        super(Side.SHADOW, 0, Culture.DUNLAND, "Running Rampant", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canDiscardCardsFromHandToPlay(self, game, self.getOwner(), 1, Culture.DUNLAND);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, PhysicalCard self, int twilightModifier) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 1, Culture.DUNLAND));
        action.appendEffect(
                new ChooseActiveCardEffect(self, playerId, "Choose a site you control", Filters.siteControlled(playerId)) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard site) {
                        action.appendEffect(
                                new ChooseAndStackCardsFromDiscardEffect(action, playerId, 1, 1, site, Culture.DUNLAND, Race.MAN));
                    }
                });
        return action;
    }
}
