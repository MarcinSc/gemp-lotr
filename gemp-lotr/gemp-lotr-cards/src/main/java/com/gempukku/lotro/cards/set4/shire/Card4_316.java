package com.gempukku.lotro.cards.set4.shire;

import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.MakeRingBearerEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 2
 * Type: Companion • Hobbit
 * Strength: 3
 * Vitality: 4
 * Resistance: 6
 * Signet: Gandalf
 * Game Text: Ring-bound. Response: If a Shadow card is about to add any number of burdens, spot Frodo and exert Sam
 * to prevent this. Response: If Frodo is killed, make Sam the Ring-bearer (resistance 5).
 */
public class Card4_316 extends AbstractCompanion {
    public Card4_316() {
        super(2, 3, 4, 5, Culture.SHIRE, Race.HOBBIT, Signet.GANDALF, "Sam", "Samwise the Brave", true);
        addKeyword(Keyword.RING_BOUND);
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isAddingBurden(effect, game, Side.SHADOW)
                && PlayConditions.canSpot(game, Filters.frodo)
                && PlayConditions.canSelfExert(self, game)) {
            final AddBurdenEffect addBurdenEffect = (AddBurdenEffect) effect;
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            addBurdenEffect.preventAll();
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.forEachKilled(game, effectResult, Filters.frodo, Filters.ringBearer)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(new MakeRingBearerEffect(self));
            return Collections.singletonList(action);
        }

        return null;
    }
}
