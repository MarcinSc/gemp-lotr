package com.gempukku.lotro.cards.set11.men;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Shadows
 * Side: Shadow
 * Culture: Men
 * Twilight Cost: 7
 * Type: Event • Archery
 * Game Text: Toil 2. (For each [MEN] character you exert when playing this, its twilight cost is -2.) Spot a [MEN]
 * minion to make the minion archery total +3.
 */
public class Card11_078 extends AbstractEvent {
    public Card11_078() {
        super(Side.SHADOW, 7, Culture.MEN, "Elevated Fire", Phase.ARCHERY);
        addKeyword(Keyword.TOIL, 2);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.MEN, CardType.MINION);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new AddUntilEndOfPhaseModifierEffect(
                        new ArcheryTotalModifier(self, Side.SHADOW, 3)));
        return action;
    }
}
