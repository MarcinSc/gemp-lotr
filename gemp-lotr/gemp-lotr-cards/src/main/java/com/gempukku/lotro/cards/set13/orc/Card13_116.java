package com.gempukku.lotro.cards.set13.orc;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;

import java.util.Collections;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 3
 * Type: Minion • Orc
 * Strength: 8
 * Vitality: 2
 * Site: 4
 * Game Text: While you can spot a Free Peoples culture token, each minion that has the title Orc Reaper is strength +2.
 */
public class Card13_116 extends AbstractMinion {
    public Card13_116() {
        super(3, 8, 2, 4, Race.ORC, Culture.ORC, "Orc Reaper");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
return Collections.singletonList(new StrengthModifier(self, Filters.name(getTitle()), new SpotCondition(Side.FREE_PEOPLE, Filters.hasAnyCultureTokens(1)), 2));
}
}
