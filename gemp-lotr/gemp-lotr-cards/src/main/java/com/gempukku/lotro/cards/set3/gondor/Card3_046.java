package com.gempukku.lotro.cards.set3.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;

/**
 * Set: Realms of Elf-lords
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Skirmish: Spot The Shards of Narsil to make a [GONDOR] companion strength +3 and damage +1.
 */
public class Card3_046 extends AbstractEvent {
    public Card3_046() {
        super(Side.FREE_PEOPLE, 0, Culture.GONDOR, "Still Sharp", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Filters.name("The Shards of Narsil"));
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseActiveCardEffect(self, playerId, "Choose GONDOR companion", Culture.GONDOR, CardType.COMPANION) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        action.insertEffect(
                                new AddUntilEndOfPhaseModifierEffect(
                                        new StrengthModifier(self, Filters.sameCard(card), 3)));
                        action.insertEffect(
                                new AddUntilEndOfPhaseModifierEffect(
                                        new KeywordModifier(self, Filters.sameCard(card), Keyword.DAMAGE)));
                    }
                });
        return action;
    }
}
