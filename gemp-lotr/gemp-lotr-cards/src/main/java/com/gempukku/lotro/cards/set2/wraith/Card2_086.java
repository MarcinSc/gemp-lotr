package com.gempukku.lotro.cards.set2.wraith;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;

/**
 * Set: Mines of Moria
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 1
 * Type: Event
 * Game Text: Skirmish: Spot a twilight Nazgul and the Ring-bearer wearing The One Ring to add 3 burdens.
 */
public class Card2_086 extends AbstractEvent {
    public Card2_086() {
        super(Side.SHADOW, 1, Culture.WRAITH, "Wraith-world", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Race.NAZGUL, Keyword.TWILIGHT)
                && game.getGameState().isWearingRing();
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new AddBurdenEffect(self.getOwner(), self, 3));
        return action;
    }
}
