package com.gempukku.lotro.cards.set6.gandalf;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;

/**
 * Set: Ents of Fangorn
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 4
 * Type: Companion • Ent
 * Strength: 8
 * Vitality: 3
 * Resistance: 6
 * Game Text: Quickbeam's twilight cost is -1 for each Ent and unbound Hobbit you can spot.
 */
public class Card6_033 extends AbstractCompanion {
    public Card6_033() {
        super(4, 8, 3, 6, Culture.GANDALF, Race.ENT, null, "Quickbeam", "Bregalad", true);
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target) {
        return -Filters.countActive(game, Filters.or(Race.ENT, Filters.and(Race.HOBBIT, Filters.unboundCompanion)))
                - game.getModifiersQuerying().getSpotBonus(game, Race.ENT);
    }
}
