package com.gempukku.lotro.cards.set4.dwarven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.ShuffleCardsFromPlayAndStackedOnItIntoDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.Set;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 2
 * Type: Event
 * Game Text: Regroup: Exert a Dwarf to shuffle a [DWARVEN] condition (and all cards stacked on it) into your draw deck.
 * Exert a minion for each card shuffled into your draw deck.
 */
public class Card4_055 extends AbstractEvent {
    public Card4_055() {
        super(Side.FREE_PEOPLE, 2, Culture.DWARVEN, "Restless Axe", Phase.REGROUP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Race.DWARF);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Race.DWARF));
        action.appendEffect(
                new ChooseActiveCardEffect(self, playerId, "Choose a DWARVEN condition", Culture.DWARVEN, CardType.CONDITION) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        action.insertEffect(
                                new ShuffleCardsFromPlayAndStackedOnItIntoDeckEffect(self, playerId, Collections.singleton(card)) {
                                    @Override
                                    protected void cardsShuffledCallback(Set<PhysicalCard> cardsShuffled) {
                                        for (int i = 0; i < cardsShuffled.size(); i++)
                                            action.appendEffect(
                                                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, CardType.MINION));
                                    }
                                });

                    }
                });
        return action;
    }
}
