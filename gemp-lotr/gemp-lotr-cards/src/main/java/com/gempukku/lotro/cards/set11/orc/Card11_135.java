package com.gempukku.lotro.cards.set11.orc;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.PreventableEffect;
import com.gempukku.lotro.logic.effects.RevealCardsFromYourHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseCardsFromHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Set: Shadows
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 7
 * Type: Minion • Troll
 * Strength: 15
 * Vitality: 3
 * Site: 5
 * Game Text: Damage +1. Fierce. To play, spot an [ORC] minion. When you play this minion, add a burden unless the Free
 * Peoples player reveals a Free Peoples event from his or her hand.
 */
public class Card11_135 extends AbstractMinion {
    public Card11_135() {
        super(7, 15, 3, 5, Race.TROLL, Culture.ORC, "Porter Troll");
        addKeyword(Keyword.DAMAGE, 1);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.ORC, CardType.MINION);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            final RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new PreventableEffect(action,
                            new AddBurdenEffect(self.getOwner(), self, 1), game.getGameState().getCurrentPlayerId(),
                            new PreventableEffect.PreventionCost() {
                                @Override
                                public Effect createPreventionCostForPlayer(final CostToEffectAction subAction, final String playerId) {
                                    return new ChooseCardsFromHandEffect(playerId, 1, 1, Side.FREE_PEOPLE, CardType.EVENT) {
                                        @Override
                                        public String getText(LotroGame game) {
                                            return "Reveal a Free Peoples event from hand";
                                        }

                                        @Override
                                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                                            subAction.insertEffect(
                                                    new RevealCardsFromYourHandEffect(self, playerId, selectedCards));
                                        }
                                    };
                                }
                            }));
            return Collections.singletonList(action);
        }
        return null;
    }
}
