package com.gempukku.lotro.cards.set12.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.PreventCardEffect;
import com.gempukku.lotro.logic.effects.PreventableCardEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 0
 * Type: Condition
 * Game Text: Spell. To play, spot a [GANDALF] Wizard. Bearer must be a companion. Limit 1 per bearer.
 * Response: If bearer is about to take a wound that would kill him or her, discard this condition to prevent that
 * wound.
 */
public class Card12_032 extends AbstractAttachable {
    public Card12_032() {
        super(Side.FREE_PEOPLE, CardType.CONDITION, 0, Culture.GANDALF, null, "Salve");
        addKeyword(Keyword.SPELL);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.GANDALF, Race.WIZARD);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(CardType.COMPANION, Filters.not(Filters.hasAttached(Filters.name(getTitle()))));
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingWounded(effect, game, self.getAttachedTo())
                && PlayConditions.canSelfDiscard(self, game)
                && Filters.exhausted.accepts(game, self.getAttachedTo())) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new PreventCardEffect((PreventableCardEffect) effect, self.getAttachedTo()));
            return Collections.singletonList(action);
        }
        return null;
    }
}
