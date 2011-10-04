package com.gempukku.lotro.cards.set1.dwarven;

import com.gempukku.lotro.cards.AbstractEvent;
import com.gempukku.lotro.cards.actions.PlayEventAction;
import com.gempukku.lotro.cards.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.cards.modifiers.StrengthModifier;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;

/**
 * Set: The Fellowship of the Ring
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Skirmish: Make a Dwarf strength +2 (or +4 if at an underground site).
 */
public class Card1_026 extends AbstractEvent {
    public Card1_026() {
        super(Side.FREE_PEOPLE, Culture.DWARVEN, "Their Halls of Stone", Phase.SKIRMISH);
    }

    @Override
    public int getTwilightCost() {
        return 0;
    }

    @Override
    public PlayEventAction getPlayCardAction(String playerId, final LotroGame game, final PhysicalCard self, int twilightModifier) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseActiveCardEffect(playerId, "Choose Dwarf", Filters.race(Race.DWARF)) {
                    @Override
                    protected void cardSelected(PhysicalCard dwarf) {
                        GameState gameState = game.getGameState();
                        int bonus = (game.getModifiersQuerying().hasKeyword(gameState, gameState.getCurrentSite(), Keyword.UNDERGROUND)) ? 4 : 2;
                        action.appendEffect(
                                new AddUntilEndOfPhaseModifierEffect(new StrengthModifier(self, Filters.sameCard(dwarf), bonus), Phase.SKIRMISH));
                    }
                }
        );
        return action;
    }
}
