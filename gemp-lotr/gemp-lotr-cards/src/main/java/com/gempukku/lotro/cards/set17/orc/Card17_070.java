package com.gempukku.lotro.cards.set17.orc;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.CardMatchesEvaluator;

/**
 * Set: Rise of Saruman
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 1
 * Type: Event • Skirmish
 * Game Text: Make an [ORC] minion strength +2 (or +4 if it is mounted).
 */
public class Card17_070 extends AbstractEvent {
    public Card17_070() {
        super(Side.SHADOW, 1, Culture.ORC, "Feral Ride", Phase.SKIRMISH);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseAndAddUntilEOPStrengthBonusEffect(
                        action, self, playerId, new CardMatchesEvaluator(2, 4, Filters.mounted), Race.ORC, CardType.MINION));
        return action;
    }
}
