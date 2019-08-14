package com.gempukku.lotro.cards.set10.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.effects.OptionalEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.effects.ShuffleDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPutCardFromDeckIntoHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseOpponentEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Mount Doom
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 2
 * Type: Companion • Man
 * Strength: 8
 * Vitality: 3
 * Resistance: 6
 * Signet: Aragorn
 * Game Text: To play, spot 2 [GONDOR] Men. Fellowship: If at a sanctuary, exert Denethor to take a [GONDOR] card into
 * hand from your draw deck, then reshuffle. Choose an opponent who may draw 2 cards.
 */
public class Card10_028 extends AbstractCompanion {
    public Card10_028() {
        super(2, 8, 3, 6, Culture.GONDOR, Race.MAN, Signet.ARAGORN, "Denethor", "Lord of Minas Tirith", true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, 2, Culture.GONDOR, Race.MAN);
    }

    @Override
    public List<ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.FELLOWSHIP, self)
                && PlayConditions.location(game, Keyword.SANCTUARY)
                && PlayConditions.canSelfExert(self, game)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new ChooseAndPutCardFromDeckIntoHandEffect(action, playerId, 1, 1, Culture.GONDOR));
            action.appendEffect(
                    new ShuffleDeckEffect(playerId));
            action.appendEffect(
                    new ChooseOpponentEffect(playerId) {
                        @Override
                        protected void opponentChosen(String opponentId) {
                            action.insertEffect(
                                    new OptionalEffect(action, opponentId,
                                            new DrawCardsEffect(action, opponentId, 2)));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
