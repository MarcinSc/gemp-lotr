package com.gempukku.lotro.cards.set4.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.ForEachYouSpotDecision;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 3
 * Type: Event
 * Game Text: Tale. Fellowship: Draw a card for each Elf companion you spot.
 */
public class Card4_058 extends AbstractEvent {
    public Card4_058() {
        super(Side.FREE_PEOPLE, 3, Culture.ELVEN, "Alliance Reforged", Phase.FELLOWSHIP);
        addKeyword(Keyword.TALE);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new PlayoutDecisionEffect(playerId,
                        new ForEachYouSpotDecision(1, "Choose number of Elf companions you wish to spot", game, Filters.and(Race.ELF, CardType.COMPANION)) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                int spotted = getValidatedResult(result);
                                action.appendEffect(
                                        new DrawCardsEffect(action, playerId, spotted));
                            }
                        }));
        return action;
    }
}
