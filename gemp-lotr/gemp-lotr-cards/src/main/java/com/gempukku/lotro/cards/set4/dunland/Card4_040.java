package com.gempukku.lotro.cards.set4.dunland;

import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.effects.TakeControlOfASiteEffect;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Shadow
 * Culture: Dunland
 * Twilight Cost: 4
 * Type: Minion • Man
 * Strength: 10
 * Vitality: 2
 * Site: 3
 * Game Text: Fierce. Response: If another [DUNLAND] Man wins a skirmish, exert Wulf to take control of a site.
 */
public class Card4_040 extends AbstractMinion {
    public Card4_040() {
        super(4, 10, 2, 3, Race.MAN, Culture.DUNLAND, "Wulf", "Dunlending Chieftain", true);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.winsSkirmish(game, effectResult, Filters.and(Filters.not(self), Culture.DUNLAND, Race.MAN))
                && PlayConditions.canExert(self, game, self)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new TakeControlOfASiteEffect(self, playerId));
            return Collections.singletonList(action);
        }
        return null;
    }
}
