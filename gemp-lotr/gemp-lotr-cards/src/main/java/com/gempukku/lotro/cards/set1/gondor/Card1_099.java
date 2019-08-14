package com.gempukku.lotro.cards.set1.gondor;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseOpponentEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Fellowship of the Ring
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Regroup: Exert a ranger to make an opponent shuffle his hand into his draw deck and draw 8 cards.
 */
public class Card1_099 extends AbstractEvent {
    public Card1_099() {
        super(Side.FREE_PEOPLE, 0, Culture.GONDOR, "Change of Plans", Phase.REGROUP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Keyword.RANGER);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, PhysicalCard self, int twilightModifier) {
        final PlayEventAction action = new PlayEventAction(self, true);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Keyword.RANGER));
        action.appendEffect(
                new ChooseOpponentEffect(playerId) {
                    @Override
                    protected void opponentChosen(String opponentId) {
                        List<PhysicalCard> hand = new LinkedList<PhysicalCard>(game.getGameState().getHand(opponentId));
                        game.getGameState().removeCardsFromZone(playerId, hand);
                        for (PhysicalCard physicalCard : hand) {
                            game.getGameState().putCardOnBottomOfDeck(physicalCard);
                        }
                        game.getGameState().shuffleDeck(opponentId);

                        action.appendEffect(new DrawCardsEffect(action, opponentId, 8));
                    }
                });
        return action;
    }
}
