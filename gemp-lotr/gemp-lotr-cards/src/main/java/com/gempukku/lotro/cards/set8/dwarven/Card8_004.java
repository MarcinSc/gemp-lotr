package com.gempukku.lotro.cards.set8.dwarven;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.ReplaceInSkirmishEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Siege of Gondor
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 2
 * Type: Event • Skirmish
 * Game Text: If Gimli is not assigned to a skirmish, add a threat to have him replace an unbound companion in
 * a skirmish.
 */
public class Card8_004 extends AbstractEvent {
    public Card8_004() {
        super(Side.FREE_PEOPLE, 2, Culture.DWARVEN, "Counts But One", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Filters.gimli, Filters.notAssignedToSkirmish)
                && PlayConditions.canAddThreat(game, self, 1);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new AddThreatsEffect(playerId, self, 1));
        action.appendEffect(
                new ChooseActiveCardEffect(self, playerId, "Choose Gimli", Filters.gimli) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        action.insertEffect(
                                new ReplaceInSkirmishEffect(card, Filters.unboundCompanion));
                    }
                });
        return action;
    }
}
