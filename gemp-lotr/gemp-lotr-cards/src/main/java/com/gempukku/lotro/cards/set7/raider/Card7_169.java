package com.gempukku.lotro.cards.set7.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 1
 * Type: Condition • Support Area
 * Game Text: Regroup: Exert an Easterling and discard this condition to make the move limit -1 for this turn.
 * The Free Peoples player may add 2 burdens to prevent this. Skirmish: Discard this condition to heal an Easterling.
 */
public class Card7_169 extends AbstractPermanent {
    public Card7_169() {
        super(Side.SHADOW, 1, CardType.CONDITION, Culture.RAIDER, "Surging Up");
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, final LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.REGROUP, self, 0)
                && PlayConditions.canExert(self, game, Keyword.EASTERLING)
                && PlayConditions.canSelfDiscard(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Keyword.EASTERLING));
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new PreventableEffect(action,
                            new AddUntilEndOfTurnModifierEffect(
                                    new MoveLimitModifier(self, -1)) {
                                @Override
                                public String getText(LotroGame game) {
                                    return "Make the move limit -1 for this turn";
                                }
                            }, game.getGameState().getCurrentPlayerId(),
                            new PreventableEffect.PreventionCost() {
                                @Override
                                public Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId) {
                                    return new AddBurdenEffect(game.getGameState().getCurrentPlayerId(), self, 2);
                                }
                            }));
            return Collections.singletonList(action);
        }
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SKIRMISH, self, 0)
                && PlayConditions.canSelfDiscard(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new ChooseAndHealCharactersEffect(action, playerId, Keyword.EASTERLING));
            return Collections.singletonList(action);
        }
        return null;
    }
}
