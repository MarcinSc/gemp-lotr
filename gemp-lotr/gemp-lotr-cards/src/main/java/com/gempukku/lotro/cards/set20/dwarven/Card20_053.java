package com.gempukku.lotro.cards.set20.dwarven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPutCardFromDiscardIntoHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndStackCardsFromHandEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * 3
 * •Gimli, Dwarven Emissary
 * Companion • Dwarf
 * 6	4	7
 * Damage +1.
 * While in your starting fellowship, Gimli's twilight cost is -1.
 * At the start of the fellowship phase, you may stack a Free Peoples card from hand on a [Dwarven] condition
 * in your support area to take a [Dwarven] event from your discard pile into hand.
 * http://lotrtcg.org/coreset/dwarven/gimlide(r1).png
 */
public class Card20_053 extends AbstractCompanion {
    public Card20_053() {
        super(3, 6, 4, 7, Culture.DWARVEN, Race.DWARF, null, "Gimli", "Dwarven Emissary", true);
        addKeyword(Keyword.DAMAGE, 1);
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target) {
        if (game.getGameState().getCurrentPhase() == Phase.PLAY_STARTING_FELLOWSHIP)
            return -1;
        return 0;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(final String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.FELLOWSHIP)
                && PlayConditions.hasCardInHand(game, playerId, 1, Side.FREE_PEOPLE)
                && PlayConditions.canSpot(game, Culture.DWARVEN, Keyword.SUPPORT_AREA, CardType.CONDITION)) {
            final OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new ChooseActiveCardEffect(self, playerId, "Choose a DWARVEN condition in your support area", Culture.DWARVEN, Keyword.SUPPORT_AREA, CardType.CONDITION) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            action.appendCost(
                                    new ChooseAndStackCardsFromHandEffect(action, playerId, 1, 1, card, Side.FREE_PEOPLE));
                        }
                    });
            action.appendEffect(
                    new ChooseAndPutCardFromDiscardIntoHandEffect(action, playerId, 1, 1, Culture.DWARVEN, CardType.EVENT));
            return Collections.singletonList(action);
        }
        return null;
    }
}
