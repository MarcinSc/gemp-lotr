package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.CancelSkirmishEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class CancelSkirmish implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "fierceOnly");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");
        boolean fierceOnly = FieldUtils.getBoolean(effectObject.get("fierceOnly"), "fierceOnly", false);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final Filterable involving = filterableSource.getFilterable(actionContext);
                if (!fierceOnly || actionContext.getGame().getGameState().isFierceSkirmishes()) {
                    return new CancelSkirmishEffect(involving);
                } else {
                    return null;
                }
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final Filterable involving = filterableSource.getFilterable(actionContext);
                final LotroGame game = actionContext.getGame();
                return game.getGameState().getSkirmish() != null
                        && (!fierceOnly || game.getGameState().isFierceSkirmishes())
                        && !game.getGameState().getSkirmish().isCancelled()
                        && (Filters.countActive(game, Filters.and(involving, Filters.inSkirmish)) > 0)
                        && game.getModifiersQuerying().canCancelSkirmish(game, game.getGameState().getSkirmish().getFellowshipCharacter())
                        && (game.getFormat().canCancelRingBearerSkirmish() || Filters.countActive(game, Filters.ringBearer, Filters.inSkirmish) == 0);
            }
        };
    }
}
