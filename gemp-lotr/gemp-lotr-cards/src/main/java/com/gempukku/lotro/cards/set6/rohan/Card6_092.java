package com.gempukku.lotro.cards.set6.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Ents of Fangorn
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 3
 * Type: Companion • Man
 * Strength: 7
 * Vitality: 3
 * Resistance: 6
 * Signet: Gandalf
 * Game Text: Valiant. While you can spot a [ROHAN] Man, Eomer's twilight cost is -1. Skirmish: Discard 3 cards from
 * hand to make a [ROHAN] Man strength +2 for each wound on each minion in his or her skirmish.
 */
public class Card6_092 extends AbstractCompanion {
    public Card6_092() {
        super(3, 7, 3, 6, Culture.ROHAN, Race.MAN, Signet.GANDALF, Names.eomer, "Rohirrim Captain", true);
        addKeyword(Keyword.VALIANT);
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target) {
        if (Filters.canSpot(game, Culture.ROHAN, Race.MAN))
            return -1;
        return 0;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canDiscardFromHand(game, playerId, 3, Filters.any)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 3));
            action.appendEffect(
                    new ChooseAndAddUntilEOPStrengthBonusEffect(action, self, playerId,
                            new Evaluator() {
                                @Override
                                public int evaluateExpression(LotroGame game, PhysicalCard self) {
                                    int wounds = 0;
                                    for (PhysicalCard woundedMinion : Filters.filterActive(game, CardType.MINION, Filters.wounded, Filters.inSkirmishAgainst(self))) {
                                        wounds += game.getGameState().getWounds(woundedMinion);
                                    }
                                    return wounds * 2;
                                }
                            }, Culture.ROHAN, Race.MAN));
            return Collections.singletonList(action);
        }
        return null;
    }
}
