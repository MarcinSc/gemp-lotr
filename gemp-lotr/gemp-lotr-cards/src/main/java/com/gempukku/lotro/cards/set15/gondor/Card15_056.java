package com.gempukku.lotro.cards.set15.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.SingleMemoryEvaluator;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Hunters
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 1
 * Type: Possession • Ranged Weapon
 * Game Text: Bearer must be Aragorn. He is an archer. Archery: Add a threat to wound a minion with the lowest strength.
 */
public class Card15_056 extends AbstractAttachableFPPossession {
    public Card15_056() {
        super(1, 0, 0, Culture.GONDOR, PossessionClass.RANGED_WEAPON, "Aragorn's Bow", "Ranger's Longbow", true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.aragorn;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new KeywordModifier(self, Filters.hasAttached(self), Keyword.ARCHER));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.ARCHERY, self)
                && PlayConditions.canAddThreat(game, self, 1)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new AddThreatsEffect(playerId, self, 1));
            action.appendEffect(
                    new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, CardType.MINION, Filters.strengthEqual(
                            new SingleMemoryEvaluator(
                                    new Evaluator() {
                                        @Override
                                        public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                            int minStrength = Integer.MAX_VALUE;
                                            for (PhysicalCard minion : Filters.filterActive(game, CardType.MINION))
                                                minStrength = Math.min(minStrength, game.getModifiersQuerying().getStrength(game, minion));
                                            return minStrength;
                                        }
                                    }
                            ))));
            return Collections.singletonList(action);
        }
        return null;
    }
}
