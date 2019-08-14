package com.gempukku.lotro.cards.set1.sauron;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: The Fellowship of the Ring
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 3
 * Type: Event
 * Game Text: Search. Maneuver: Spot a [SAURON] Orc and 6 companions to wound a companion (except the Ring-bearer).
 * Do this once for each companion over 5.
 */
public class Card1_251 extends AbstractEvent {
    public Card1_251() {
        super(Side.SHADOW, 3, Culture.SAURON, "A Host Avails Little", Phase.MANEUVER);
        addKeyword(Keyword.SEARCH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Culture.SAURON, Race.ORC)
                && PlayConditions.canSpot(game, 6, CardType.COMPANION);
    }

    @Override
    public PlayEventAction getPlayCardAction(final String playerId, LotroGame game, final PhysicalCard self, int twilightModifier, boolean ignoreRoamingPenalty) {
        final PlayEventAction action = new PlayEventAction(self);
        int companionCount = Filters.countActive(game, CardType.COMPANION);
        int woundCount = companionCount - 5;
        for (int i = 0; i < woundCount; i++) {
            action.appendEffect(
                    new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, CardType.COMPANION, Filters.not(Filters.ringBearer)));
        }
        return action;
    }
}
