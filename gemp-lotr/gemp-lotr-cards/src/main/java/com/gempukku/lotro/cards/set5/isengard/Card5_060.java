package com.gempukku.lotro.cards.set5.isengard;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Battle of Helm's Deep
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 0
 * Type: Condition
 * Game Text: Machine. Plays to your support area. Shadow: Play an Uruk-hai to place an [ISENGARD] token on a machine.
 * Response: If one or more machines are about to be discarded by an opponent, discard this condition to prevent that.
 */
public class Card5_060 extends AbstractPermanent {
    public Card5_060() {
        super(Side.SHADOW, 0, CardType.CONDITION, Culture.ISENGARD, "Siege Engine");
        addKeyword(Keyword.MACHINE);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SHADOW, self, 0)
                && PlayConditions.canPlayFromHand(playerId, game, Race.URUK_HAI)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndPlayCardFromHandEffect(playerId, game, Race.URUK_HAI));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose a machine", Keyword.MACHINE) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            action.insertEffect(
                                    new AddTokenEffect(self, card, Token.ISENGARD));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingDiscardedByOpponent(effect, game, playerId, Keyword.MACHINE)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new PreventCardEffect((PreventableCardEffect) effect, Keyword.MACHINE));

            return Collections.singletonList(action);
        }
        return null;
    }
}
