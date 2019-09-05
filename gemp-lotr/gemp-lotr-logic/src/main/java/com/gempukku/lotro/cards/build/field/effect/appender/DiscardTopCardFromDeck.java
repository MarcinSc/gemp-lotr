package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.DiscardTopCardFromDeckEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class DiscardTopCardFromDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "deck", "count", "forced");

        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");
        final int count = FieldUtils.getInteger(effectObject.get("count"), "count", 1);
        final boolean forced = FieldUtils.getBoolean(effectObject.get("forced"), "forced");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck, environment);

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String deckId = playerSource.getPlayer(actionContext);

                // Don't check if can discard top cards, since it's a cost
                final LotroGame game = actionContext.getGame();
                return game.getGameState().getDeck(deckId).size() >= count
                        && (!forced || game.getModifiersQuerying().canDiscardCardsFromTopOfDeck(game, actionContext.getPerformingPlayer(), actionContext.getSource()));
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String deckId = playerSource.getPlayer(actionContext);

                return new DiscardTopCardFromDeckEffect(actionContext.getSource(), deckId, count, forced);
            }
        };
    }

}