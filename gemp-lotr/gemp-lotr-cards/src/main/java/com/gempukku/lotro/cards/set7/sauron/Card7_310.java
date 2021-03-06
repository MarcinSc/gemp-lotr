package com.gempukku.lotro.cards.set7.sauron;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.RemoveThreatsEffect;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromHandEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 0
 * Type: Condition • Support Area
 * Game Text: To play, spot a [SAURON] Orc. Shadow: Remove a threat to play a [SAURON] Orc without paying any roaming
 * penalty. Regroup: Spot a [SAURON] Orc and remove (4) to add a threat.
 */
public class Card7_310 extends AbstractPermanent {
    public Card7_310() {
        super(Side.SHADOW, 0, CardType.CONDITION, Culture.SAURON, "Sauron's Hatred");
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.SAURON, Race.ORC);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SHADOW, self, 0)
                && PlayConditions.canRemoveThreat(game, self, 1)
                && PlayConditions.canPlayFromHand(playerId, game, 0, true, Culture.SAURON, Race.ORC)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveThreatsEffect(self, 1));
            action.appendEffect(
                    new ChooseAndPlayCardFromHandEffect(playerId, game, 0, true, Culture.SAURON, Race.ORC));
            return Collections.singletonList(action);
        }
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.REGROUP, self, 4)
                && PlayConditions.canSpot(game, Culture.SAURON, Race.ORC)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveTwilightEffect(4));
            action.appendEffect(
                    new AddThreatsEffect(playerId, self, 1));
            return Collections.singletonList(action);
        }
        return null;
    }
}
