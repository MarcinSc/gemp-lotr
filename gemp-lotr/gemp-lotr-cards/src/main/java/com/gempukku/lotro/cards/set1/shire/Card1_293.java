package com.gempukku.lotro.cards.set1.shire;

import com.gempukku.lotro.cards.AbstractLotroCardBlueprint;
import com.gempukku.lotro.cards.PlayConditions;
import com.gempukku.lotro.cards.actions.PlayEventAction;
import com.gempukku.lotro.cards.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.cards.effects.ExertCharacterEffect;
import com.gempukku.lotro.cards.modifiers.StrengthModifier;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Fellowship of the Ring
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Skirmish: Exert a Hobbit to make him strength +3.
 */
public class Card1_293 extends AbstractLotroCardBlueprint {
    public Card1_293() {
        super(Side.FREE_PEOPLE, CardType.EVENT, Culture.SHIRE, "Halfling Deftness");
        addKeyword(Keyword.SKIRMISH);
    }

    @Override
    public int getTwilightCost() {
        return 0;
    }

    @Override
    public List<? extends Action> getInPlayPhaseActions(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canPlayFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && Filters.canSpot(game.getGameState(), game.getModifiersQuerying(), Filters.keyword(Keyword.HOBBIT), Filters.canExert())) {
            final PlayEventAction action = new PlayEventAction(self);
            action.addCost(
                    new ChooseActiveCardEffect(playerId, "Choose a Hobbit", Filters.keyword(Keyword.HOBBIT), Filters.canExert()) {
                        @Override
                        protected void cardSelected(PhysicalCard hobbit) {
                            action.addCost(new ExertCharacterEffect(hobbit));
                            action.addEffect(
                                    new AddUntilEndOfPhaseModifierEffect(
                                            new StrengthModifier(self, Filters.sameCard(hobbit), 3), Phase.SKIRMISH));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
