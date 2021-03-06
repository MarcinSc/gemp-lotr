package com.gempukku.lotro.cards.set6.gondor;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.ChooseAndHealCharactersEffect;
import com.gempukku.lotro.logic.effects.LiberateASiteEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Ents of Fangorn
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 1
 * Type: Possession • Hand Weapon
 * Game Text: Bearer must be a [GONDOR] companion. Each time bearer wins a skirmish, you may heal another
 * [GONDOR] companion. Response: If bearer wins a skirmish, discard this possession to liberate a site.
 */
public class Card6_051 extends AbstractAttachableFPPossession {
    public Card6_051() {
        super(1, 0, 0, Culture.GONDOR, PossessionClass.HAND_WEAPON, "Banner of Westernesse");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.GONDOR, CardType.COMPANION);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.winsSkirmish(game, effectResult, self.getAttachedTo())) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.setText("Heal another GONDOR companion");
            action.appendEffect(
                    new ChooseAndHealCharactersEffect(action, playerId, Culture.GONDOR, CardType.COMPANION, Filters.not(self.getAttachedTo())));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.winsSkirmish(game, effectResult, self.getAttachedTo())
                && PlayConditions.canSelfDiscard(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.setText("Discard to liberate a site");
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new LiberateASiteEffect(self, playerId, null));
            return Collections.singletonList(action);
        }
        return null;
    }
}
