package com.gempukku.lotro.cards.set20.rohan;

import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.ConditionEvaluator;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.SpotCondition;

/**
 * 0
 * Wall Never Breached
 * Rohan	Event • Skirmish
 * Make a [Rohan] companion strength +2 (or strength +3 if you can spot a fortification).
 */
public class Card20_348 extends AbstractEvent {
    public Card20_348() {
        super(Side.FREE_PEOPLE, 0, Culture.ROHAN, "Wall Never Breached", Phase.SKIRMISH);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseAndAddUntilEOPStrengthBonusEffect(action, self, playerId,
                        new ConditionEvaluator(2, 3, new SpotCondition(Keyword.FORTIFICATION)), Culture.ROHAN, CardType.COMPANION));
        return action;
    }
}
