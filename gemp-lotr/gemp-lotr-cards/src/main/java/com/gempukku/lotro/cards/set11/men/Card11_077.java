package com.gempukku.lotro.cards.set11.men;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.condition.LocationCondition;

import java.util.Collections;
import java.util.List;

/**
 * Set: Shadows
 * Side: Shadow
 * Culture: Men
 * Twilight Cost: 4
 * Type: Minion • Man
 * Strength: 10
 * Vitality: 3
 * Site: 4
 * Game Text: While this minion is at a plains site, it is damage +1.
 */
public class Card11_077 extends AbstractMinion {
    public Card11_077() {
        super(4, 10, 3, 4, Race.MAN, Culture.MEN, "Elder of Dunland");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(new KeywordModifier(self, self, new LocationCondition(Keyword.PLAINS), Keyword.DAMAGE, 1));
    }
}
