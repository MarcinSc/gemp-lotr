package com.gempukku.lotro.cards.set5.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Battle of Helm's Deep
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 3
 * Type: Event
 * Game Text: Spell. Maneuver: Spot 3 twilight tokens and exert Gandalf to wound a minion twice.
 */
public class Card5_018 extends AbstractEvent {
    public Card5_018() {
        super(Side.FREE_PEOPLE, 3, Culture.GANDALF, "Fury of the White Rider", Phase.MANEUVER);
        addKeyword(Keyword.SPELL);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Filters.gandalf)
                && game.getGameState().getTwilightPool() >= 3;
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Filters.gandalf));
        action.appendEffect(
                new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, 2, CardType.MINION));
        return action;
    }
}
