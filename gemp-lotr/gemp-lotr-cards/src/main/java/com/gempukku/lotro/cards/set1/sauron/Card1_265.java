package com.gempukku.lotro.cards.set1.sauron;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractResponseEvent;
import com.gempukku.lotro.logic.effects.DiscardTopCardFromDeckEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Fellowship of the Ring
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Response: If a companion is killed by a [SAURON] Orc, the Free Peoples player must discard 3 cards from
 * the top of his draw deck for each card in the dead pile.
 */
public class Card1_265 extends AbstractResponseEvent {
    public Card1_265() {
        super(Side.SHADOW, 0, Culture.SAURON, "Orc Butchery");
    }

    @Override
    public List<PlayEventAction> getOptionalInHandAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.forEachKilledBy(game, effectResult, Filters.and(Culture.SAURON, Race.ORC), CardType.COMPANION)
                && PlayUtils.checkPlayRequirements(game, self, Filters.any, 0, 0, false, false)) {
            PlayEventAction action = new PlayEventAction(self);
            int deadPileCount = game.getGameState().getDeadPile(game.getGameState().getCurrentPlayerId()).size();
            if (deadPileCount > 0)
                action.appendEffect(new DiscardTopCardFromDeckEffect(self, game.getGameState().getCurrentPlayerId(), deadPileCount * 3, true));
            return Collections.singletonList(action);
        }
        return null;
    }
}
