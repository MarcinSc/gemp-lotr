package com.gempukku.lotro.cards.set1.site;

import com.gempukku.lotro.cards.AbstractSite;
import com.gempukku.lotro.cards.modifiers.RoamingPenaltyModifier;
import com.gempukku.lotro.common.Block;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ModifiersQuerying;

/**
 * Set: The Fellowship of the Ring
 * Twilight Cost: 1
 * Type: Site
 * Site: 2
 * Game Text: The roaming penalty for each Nazgul you play to Bree Streets is -2.
 */
public class Card1_328 extends AbstractSite {
    public Card1_328() {
        super("Bree Streets", Block.FELLOWSHIP, 2, 1, Direction.LEFT);
    }

    @Override
    public Modifier getAlwaysOnModifier(final PhysicalCard self) {
        return new RoamingPenaltyModifier(self,
                Filters.and(
                        Filters.race(Race.NAZGUL),
                        new Filter() {
                            @Override
                            public boolean accepts(GameState gameState, ModifiersQuerying modifiersQuerying, PhysicalCard physicalCard) {
                                return (gameState.getCurrentSite() == self);
                            }
                        }), -2);
    }
}
