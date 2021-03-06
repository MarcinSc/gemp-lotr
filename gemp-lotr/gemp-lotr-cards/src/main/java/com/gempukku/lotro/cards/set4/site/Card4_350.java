package com.gempukku.lotro.cards.set4.site;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractSite;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Twilight Cost: 8
 * Type: Site
 * Site: 5T
 * Game Text: Battleground. While you can spot Aragorn, the Shadow number of Hornburg Courtyard is -2.
 */
public class Card4_350 extends AbstractSite {
    public Card4_350() {
        super("Hornburg Courtyard", SitesBlock.TWO_TOWERS, 5, 8, Direction.LEFT);
        addKeyword(Keyword.BATTLEGROUND);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new TwilightCostModifier(self, self, new SpotCondition(Filters.aragorn), -2));
    }
}
