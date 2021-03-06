package com.gempukku.lotro.cards.set6.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Ents of Fangorn
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 2
 * Type: Condition
 * Strength: +4
 * Game Text: Bearer must be an Ent. Limit 1 per bearer. Discard this condition at the end of the turn.
 */
public class Card6_026 extends AbstractAttachable {
    public Card6_026() {
        super(Side.FREE_PEOPLE, CardType.CONDITION, 2, Culture.GANDALF, null, "Enraged");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Race.ENT, Filters.not(Filters.hasAttached(Filters.name(getTitle()))));
    }

    @Override
    public int getStrength() {
        return 4;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.endOfTurn(game, effectResult)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new SelfDiscardEffect(self));
            return Collections.singletonList(action);
        }
        return null;
    }
}
