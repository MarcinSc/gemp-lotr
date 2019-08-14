package com.gempukku.lotro.cards.set1.moria;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.ChooseArbitraryCardsEffect;
import com.gempukku.lotro.logic.effects.StackCardFromPlayEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Set: The Fellowship of the Ring
 * Side: Shadow
 * Culture: Moria
 * Twilight Cost: 1
 * Type: Condition
 * Game Text: Plays to your support area. Response: If your [MORIA] Orc wins a skirmish, discard cards and wounds on
 * that Orc and stack that Orc on this condition. Shadow: Play an Orc stacked here as if played from hand.
 */
public class Card1_183 extends AbstractPermanent {
    public Card1_183() {
        super(Side.SHADOW, 1, CardType.CONDITION, Culture.MORIA, "Goblin Swarms");
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(final String playerId, final LotroGame game, PhysicalCard self) {
        List<PhysicalCard> stackedCards = game.getGameState().getStackedCards(self);
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SHADOW, self, 0)
                && Filters.filter(stackedCards, game, Filters.playable(game)).size() > 0) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(
                    new ChooseArbitraryCardsEffect(playerId, "Choose an Orc to play", stackedCards, Filters.playable(game), 1, 1) {
                        @Override
                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> stackedOrcs) {
                            if (stackedOrcs.size() > 0) {
                                PhysicalCard stackedOrc = stackedOrcs.iterator().next();
                                game.getActionsEnvironment().addActionToStack(PlayUtils.getPlayCardAction(game, stackedOrc, 0, Filters.any, false));
                            }
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        if (TriggerConditions.winsSkirmish(game, effectResult, Filters.and(Culture.MORIA, Race.ORC))
                && Filters.canSpot(game, Culture.MORIA, Race.ORC, Filters.inSkirmish)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose a MORIA Orc", Culture.MORIA, Race.ORC, Filters.inSkirmish) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard moriaOrc) {
                            action.appendEffect(new StackCardFromPlayEffect(moriaOrc, self));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
