package com.gempukku.lotro.cards.set7.raider;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.PreventableEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 3
 * Type: Minion • Man
 * Strength: 8
 * Vitality: 2
 * Site: 4
 * Game Text: Southron. Maneuver: Exert this minion to add (5). The Free Peoples player may add a threat
 * to prevent this.
 */
public class Card7_160 extends AbstractMinion {
    public Card7_160() {
        super(3, 8, 2, 4, Race.MAN, Culture.RAIDER, "Southron Bandit");
        addKeyword(Keyword.SOUTHRON);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.MANEUVER, self, 0)
                && PlayConditions.canSelfExert(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new PreventableEffect(action,
                            new AddTwilightEffect(self, 5) {
                                @Override
                                public String getText(LotroGame game) {
                                    return "Add (5)";
                                }
                            }, game.getGameState().getCurrentPlayerId(),
                            new PreventableEffect.PreventionCost() {
                                @Override
                                public Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId) {
                                    return new AddThreatsEffect(playerId, self, 1) {
                                        @Override
                                        public String getText(LotroGame game) {
                                            return "Add a threat";
                                        }
                                    };
                                }
                            }
                    ));
            return Collections.singletonList(action);
        }
        return null;
    }
}
