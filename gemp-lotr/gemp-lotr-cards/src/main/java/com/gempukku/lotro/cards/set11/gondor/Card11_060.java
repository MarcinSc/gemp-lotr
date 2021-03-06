package com.gempukku.lotro.cards.set11.gondor;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;

import java.util.Set;

/**
 * Set: Shadows
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 2
 * Type: Event • Skirmish
 * Game Text: Exert any number of [GONDOR] companions who have total resistance 12 or more to make a minion skirmishing
 * a [GONDOR] companion strength -3 for each companion exerted this way.
 */
public class Card11_060 extends AbstractEvent {
    public Card11_060() {
        super(Side.FREE_PEOPLE, 2, Culture.GONDOR, "The Highest Quality", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return canExertMinResistanceGondorCompanions(game, self, 12);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new PlayoutDecisionEffect(playerId,
                        new CardsSelectionDecision(1, "Choose GONDOR companions to exert with a total resistance of 12 or more", Filters.filterActive(game, Culture.GONDOR, CardType.COMPANION, Filters.canExert(self)), 1, Integer.MAX_VALUE) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Set<PhysicalCard> characters = getSelectedCardsByResponse(result);
                                int resistanceTotal = 0;
                                for (PhysicalCard character : characters)
                                    resistanceTotal += game.getModifiersQuerying().getResistance(game, character);
                                if (resistanceTotal < 12)
                                    throw new DecisionResultInvalidException("These characters have only " + resistanceTotal + " resistance total");

                                action.insertCost(
                                        new ExertCharactersEffect(action, self, characters.toArray(new PhysicalCard[characters.size()])));
                                action.appendEffect(
                                        new ChooseAndAddUntilEOPStrengthBonusEffect(action, self, playerId, -3 * characters.size(), CardType.MINION, Filters.inSkirmishAgainst(Culture.GONDOR, CardType.COMPANION)));
                            }
                        }));
        return action;
    }

    private boolean canExertMinResistanceGondorCompanions(LotroGame game, PhysicalCard self, int resistance) {
        int resistanceTotal = 0;
        for (PhysicalCard physicalCard : Filters.filterActive(game, CardType.COMPANION, Culture.GONDOR, Filters.canExert(self))) {
            resistanceTotal += game.getModifiersQuerying().getResistance(game, physicalCard);
        }
        return resistanceTotal >= resistance;
    }
}
