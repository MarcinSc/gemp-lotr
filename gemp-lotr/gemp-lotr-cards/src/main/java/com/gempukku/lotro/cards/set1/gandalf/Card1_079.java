package com.gempukku.lotro.cards.set1.gandalf;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Fellowship of the Ring
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 1
 * Type: Condition
 * Game Text: To play, spot Gandalf. Plays to your support area. Each time you play a companion whose race you cannot
 * spot, that companion's twilight cost is -2.
 */
public class Card1_079 extends AbstractPermanent {
    public Card1_079() {
        super(Side.FREE_PEOPLE, 1, CardType.CONDITION, Culture.GANDALF, "The Nine Walkers");
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Filters.gandalf);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(new TwilightCostModifier(self,
                Filters.and(
                        CardType.COMPANION,
                        new Filter() {
                            @Override
                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                final Race race = physicalCard.getBlueprint().getRace();
                                if (race != null) {
                                    return !Filters.canSpot(game, race);
                                }
                                return false;
                            }
                        }
                ), -2));
    }
}
