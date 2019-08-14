package com.gempukku.lotro.cards.set40.sauron;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Title: Shadow in the East
 * Set: Second Edition
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 1
 * Type: Event - Maneuver
 * Card Number: 1C239
 * Game Text: Spot a [SAURON] minion to add a threat. Add an additional threat for each Free Peoples culture less than 4 you can spot.
 */
public class Card40_239 extends AbstractEvent {
    public Card40_239() {
        super(Side.SHADOW, 1, Culture.SAURON, "Shadow in the East", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.SAURON, CardType.MINION);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier) {
        PlayEventAction action = new PlayEventAction(self);
        int fpCulturesCount = GameUtils.getSpottableFPCulturesCount(game, playerId);
        int threatCount = 1 + Math.max(0, 4 - fpCulturesCount);
        action.appendEffect(
                new AddThreatsEffect(playerId, self, threatCount));
        return action;
    }
}
