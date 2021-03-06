package com.gempukku.lotro.cards.set7.wraith;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.condition.AndCondition;
import com.gempukku.lotro.logic.modifiers.condition.NotCondition;
import com.gempukku.lotro.logic.modifiers.condition.PhaseCondition;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 2
 * Type: Minion • Orc
 * Strength: 7
 * Vitality: 1
 * Site: 4
 * Game Text: While you can spot a Nazgul, this minion may only take wounds during skirmish phases.
 */
public class Card7_198 extends AbstractMinion {
    public Card7_198() {
        super(2, 7, 1, 4, Race.ORC, Culture.WRAITH, "Morgul Ruffian");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new CantTakeWoundsModifier(self,
                        new AndCondition(new SpotCondition(Race.NAZGUL), new NotCondition(new PhaseCondition(Phase.SKIRMISH))),
                        self));
    }
}
