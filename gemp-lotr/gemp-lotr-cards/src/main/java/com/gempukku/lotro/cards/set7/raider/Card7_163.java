package com.gempukku.lotro.cards.set7.raider;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddUntilEndOfTurnModifierEffect;
import com.gempukku.lotro.logic.modifiers.HasInitiativeModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 4
 * Type: Minion • Man
 * Strength: 9
 * Vitality: 2
 * Site: 4
 * Game Text: Southron. To play, spot a [RAIDER] Man. Each time the Free Peoples player assigns this minion
 * to a character, the Shadow has initiative until the end of the turn.
 */
public class Card7_163 extends AbstractMinion {
    public Card7_163() {
        super(4, 9, 2, 4, Race.MAN, Culture.RAIDER, "Southron Chieftain");
        addKeyword(Keyword.SOUTHRON);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.RAIDER, Race.MAN);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.assignedAgainst(game, effectResult, Side.FREE_PEOPLE, Filters.any, self)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new AddUntilEndOfTurnModifierEffect(
                            new HasInitiativeModifier(self, null, Side.SHADOW)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
