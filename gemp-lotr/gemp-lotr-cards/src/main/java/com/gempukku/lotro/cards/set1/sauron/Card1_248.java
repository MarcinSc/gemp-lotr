package com.gempukku.lotro.cards.set1.sauron;

import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;

/**
 * Set: The Fellowship of the Ring
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Shadow: Spot X [SAURON] minions to add (X).
 */
public class Card1_248 extends AbstractEvent {
    public Card1_248() {
        super(Side.SHADOW, 0, Culture.SAURON, "Forces of Mordor", Phase.SHADOW);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, final PhysicalCard self, int twilightModifier) {
        final PlayEventAction action = new PlayEventAction(self);
        int sauronMinions = Filters.countActive(game, Culture.SAURON, CardType.MINION);
        if (sauronMinions > 0)
            action.appendEffect(
                    new PlayoutDecisionEffect(playerId,
                            new IntegerAwaitingDecision(1, "Choose number of minions to spot", 0, sauronMinions, sauronMinions) {
                                @Override
                                public void decisionMade(String result) throws DecisionResultInvalidException {
                                    int validatedResult = getValidatedResult(result);
                                    if (validatedResult > 0)
                                    action.appendEffect(
                                            new AddTwilightEffect(self, validatedResult));
                                }
                            }
                    ));
        return action;
    }
}
