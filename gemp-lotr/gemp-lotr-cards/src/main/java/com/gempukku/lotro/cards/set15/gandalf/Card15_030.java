package com.gempukku.lotro.cards.set15.gandalf;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Hunters
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 7
 * Type: Companion • Ent
 * Strength: 8
 * Vitality: 4
 * Resistance: 6
 * Game Text: Leaflock’s twilight cost is -1 for each Ent you can spot. While you can spot 4 other Ents, Leaflock is strength +4.
 */
public class Card15_030 extends AbstractCompanion {
    public Card15_030() {
        super(7, 8, 4, 6, Culture.GANDALF, Race.ENT, null, "Leaflock", "Finglas", true);
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target) {
        return -Filters.countSpottable(game, Race.ENT);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, final PhysicalCard self) {
        return Collections.singletonList(new StrengthModifier(self, self,
                // All Ents, minus itself
                new SpotCondition(5, Race.ENT), 4));
    }
}
