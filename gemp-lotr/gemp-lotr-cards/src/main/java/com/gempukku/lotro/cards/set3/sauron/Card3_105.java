package com.gempukku.lotro.cards.set3.sauron;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.PreventableEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collections;

/**
 * Set: Realms of Elf-lords
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Maneuver: Spot a [SAURON] minion to discard Bilbo. The Free Peoples player may discard 2 Free Peoples
 * conditions to prevent this.
 */
public class Card3_105 extends AbstractEvent {
    public Card3_105() {
        super(Side.SHADOW, 0, Culture.SAURON, "Why Shouldn't I Keep It?", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Culture.SAURON, CardType.MINION);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, final LotroGame game, PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new PreventableEffect(action,
                        new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, Filters.name("Bilbo")) {
                            @Override
                            public String getText(LotroGame game) {
                                return "Discard Bilbo";
                            }
                        }, Collections.singletonList(game.getGameState().getCurrentPlayerId()),
                        new PreventableEffect.PreventionCost() {
                            @Override
                            public Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId) {
                                return new ChooseAndDiscardCardsFromPlayEffect(subAction, playerId, 2, 2, Side.FREE_PEOPLE, CardType.CONDITION);
                            }
                        }
                ));
        return action;
    }
}
