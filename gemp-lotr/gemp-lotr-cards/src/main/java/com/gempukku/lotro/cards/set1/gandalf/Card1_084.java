package com.gempukku.lotro.cards.set1.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: The Fellowship of the Ring
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 3
 * Type: Event
 * Game Text: Spell. Fellowship: Exert Gandalf to discard every condition.
 */
public class Card1_084 extends AbstractEvent {
    public Card1_084() {
        super(Side.FREE_PEOPLE, 3, Culture.GANDALF, "Sleep Caradhras", Phase.FELLOWSHIP);
        addKeyword(Keyword.SPELL);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Filters.gandalf);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier) {
        PlayEventAction action = new PlayEventAction(self);
        PhysicalCard gandalf = Filters.findFirstActive(game, Filters.gandalf);
        action.appendCost(new ExertCharactersEffect(action, self, gandalf));
        action.appendEffect(
                new DiscardCardsFromPlayEffect(self.getOwner(), self, CardType.CONDITION));
        return action;
    }
}
