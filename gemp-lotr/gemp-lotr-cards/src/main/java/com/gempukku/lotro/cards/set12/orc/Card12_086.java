package com.gempukku.lotro.cards.set12.orc;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.ForEachYouSpotEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 2
 * Type: Possession • Hand Weapon
 * Strength: +3
 * Game Text: Bearer must be an [ORC] Troll. When you play this possession, the Free Peoples player must exert
 * a companion for each lurker you spot.
 */
public class Card12_086 extends AbstractAttachable {
    public Card12_086() {
        super(Side.SHADOW, CardType.POSSESSION, 2, Culture.ORC, PossessionClass.HAND_WEAPON, "Cave Troll's Hammer", "Unwieldy Cudgel", true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.ORC, Race.TROLL);
    }

    @Override
    public int getStrength() {
        return 3;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(final LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            final RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new ForEachYouSpotEffect(self.getOwner(), Keyword.LURKER) {
                        @Override
                        protected void spottedCards(int spotCount) {
                            for (int i = 0; i < spotCount; i++)
                                action.appendEffect(
                                        new ChooseAndExertCharactersEffect(action, game.getGameState().getCurrentPlayerId(), 1, 1, CardType.COMPANION));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
