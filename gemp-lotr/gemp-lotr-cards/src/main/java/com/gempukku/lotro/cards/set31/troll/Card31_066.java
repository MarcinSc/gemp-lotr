package com.gempukku.lotro.cards.set31.troll;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.modifiers.cost.DiscardFromPlayExtraPlayCostModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Set: The Short Rest
 * Side: Shadow
 * Culture: Troll
 * Twilight Cost: 5
 * Type: Minion • Troll
 * Strength: 11
 * Vitality: 4
 * Site: 2
 * Game Text: Fierce. To play, discard an Orc. The twilight cost of each Troll is -2. Each time a companion loses
 * a skirmish, each Troll is strength +1 until the regroup phase.
 */
public class Card31_066 extends AbstractMinion {
    public Card31_066() {
        super(5, 11, 4, 2, Race.TROLL, Culture.GUNDABAD, "Tom", "Troll of Ettenmoors", true);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public List<? extends AbstractExtraPlayCostModifier> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new DiscardFromPlayExtraPlayCostModifier(self, self, 1, null, Race.ORC));
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Arrays.asList(
                new TwilightCostModifier(self, Race.TROLL, -2));
	}

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.losesSkirmish(game, effectResult, CardType.COMPANION)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new AddUntilStartOfPhaseModifierEffect(
                            new StrengthModifier(self, Race.TROLL, 1), Phase.REGROUP));
            return Collections.singletonList(action);
        }
        return null;
	}
}