package com.gempukku.lotro.cards.set1.isengard;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.List;

/**
 * et: The Fellowship of the Ring
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 2
 * Type: Condition
 * Game Text: Search. To play, spot an Uruk-hai. Plays to your support area. While the Ring-bearer is exhausted or you
 * can spot 5 burdens, the move limit for this turn is -1 (to a minimum of 1).
 */
public class Card1_142 extends AbstractPermanent {
    public Card1_142() {
        super(Side.SHADOW, 2, CardType.CONDITION, Culture.ISENGARD, "Traitor's Voice");
        addKeyword(Keyword.SEARCH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Race.URUK_HAI);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(self).getUsedLimit() < 1
                && (game.getGameState().getBurdens() >= 5 || Filters.canSpot(game, Filters.ringBearer, Filters.exhausted))) {
            game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                    new MoveLimitModifier(self,-1));
            game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(self).incrementToLimit(1, 1);
        }
        return null;

    }
}
