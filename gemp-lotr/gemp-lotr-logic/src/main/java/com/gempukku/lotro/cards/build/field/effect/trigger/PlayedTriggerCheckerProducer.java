package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;
import org.json.simple.JSONObject;

public class PlayedTriggerCheckerProducer implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize");

        final String filterString = FieldUtils.getString(value.get("filter"), "filter");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        final FilterableSource filter = environment.getFilterFactory().generateFilter(filterString);
        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filter.getFilterable(actionContext);
                final boolean played = TriggerConditions.played(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (played && memorize != null) {
                    PhysicalCard playedCard = ((PlayCardResult) actionContext.getEffectResult()).getPlayedCard();
                    actionContext.setCardMemory(memorize, playedCard);
                }
                return played;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
