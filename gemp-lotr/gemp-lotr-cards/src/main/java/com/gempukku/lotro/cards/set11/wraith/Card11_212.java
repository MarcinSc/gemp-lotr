package com.gempukku.lotro.cards.set11.wraith;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.TransferPermanentEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.CountActiveEvaluator;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Shadows
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 1
 * Type: Condition • Support Area
 * Game Text: Skirmish: Transfer this condition from your support area to a companion your Nazgul is skirmishing.
 * Limit 1 per companion. Each [WRAITH] minion skirmishing bearer is strength +1 for each forest site you can spot.
 */
public class Card11_212 extends AbstractPermanent {
    public Card11_212() {
        super(Side.SHADOW, 1, CardType.CONDITION, Culture.WRAITH, "Lost in the Woods");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
return Collections.singletonList(new StrengthModifier(self, Filters.and(Culture.WRAITH, CardType.MINION, Filters.inSkirmishAgainst(Filters.hasAttached(self))), null, new CountActiveEvaluator(CardType.SITE, Keyword.FOREST)));
}

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SKIRMISH, self, 0)
                && self.getZone() == Zone.SUPPORT) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose a companion", CardType.COMPANION, Filters.inSkirmishAgainst(Filters.owner(playerId), Race.NAZGUL), Filters.not(Filters.hasAttached(Filters.name(getTitle())))) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard target) {
                            action.appendEffect(
                                    new TransferPermanentEffect(self, target));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
