package com.gempukku.lotro.cards.set1.sauron;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: The Fellowship of the Ring
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 2
 * Type: Event
 * Game Text: Search. Maneuver: Spot a [SAURON] Orc and 5 companions to make the Free Peoples player exert a companion
 * for each companion over 4.
 */
public class Card1_239 extends AbstractEvent {
    public Card1_239() {
        super(Side.SHADOW, 2, Culture.SAURON, "All Thought Bent on It", Phase.MANEUVER);
        addKeyword(Keyword.SEARCH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.SAURON, Race.ORC)
                && PlayConditions.canSpot(game, 5, CardType.COMPANION);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, PhysicalCard self, int twilightModifier) {
        final PlayEventAction action = new PlayEventAction(self);
        int companionCount = Filters.countActive(game, CardType.COMPANION);
        for (int i = 0; i < companionCount - 4; i++)
            action.appendEffect(
                    new ChooseAndExertCharactersEffect(action, game.getGameState().getCurrentPlayerId(), 1, 1, CardType.COMPANION));
        return action;
    }
}
