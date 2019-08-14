package com.gempukku.lotro.cards.set1.moria;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.PutCardFromDeckIntoHandOrDiscardEffect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Fellowship of the Ring
 * Side: Shadow
 * Culture: Moria
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Shadow: Spot a Hobbit (except the Ring-bearer) to reveal cards from the top of your draw deck until you
 * reveal one that is not a [MORIA] minion. Take the revealed cards into hand.
 */
public class Card1_170 extends AbstractEvent {
    public Card1_170() {
        super(Side.SHADOW, 0, Culture.MORIA, "Fool of a Took!", Phase.SHADOW);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Race.HOBBIT, Filters.not(Filters.ringBearer));
    }

    @Override
    public PlayEventAction getPlayCardAction(final String playerId, LotroGame game, final PhysicalCard self, int twilightModifier, boolean ignoreRoamingPenalty) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    public void doPlayEffect(LotroGame game) {
                        List<? extends PhysicalCard> deck = game.getGameState().getDeck(playerId);
                        List<PhysicalCard> cardsToPutIntoHand = new LinkedList<PhysicalCard>();
                        for (PhysicalCard physicalCard : deck) {
                            cardsToPutIntoHand.add(physicalCard);
                            LotroCardBlueprint blueprint = physicalCard.getBlueprint();
                            if (blueprint.getCulture() != Culture.MORIA || blueprint.getCardType() != CardType.MINION)
                                break;
                        }
                        for (PhysicalCard physicalCard : cardsToPutIntoHand) {

                        }
                        for (PhysicalCard cardToPutIntoHand : cardsToPutIntoHand) {
                            action.appendEffect(
                                    new PutCardFromDeckIntoHandOrDiscardEffect(cardToPutIntoHand));
                        }
                    }
                });

        return action;
    }
}
