package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SubCostToEffectAction;
import com.gempukku.lotro.logic.effects.PutCardFromStackedIntoHandEffect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;

public class ChooseAndPutCardFromStackedToHandEffect extends ChooseStackedCardsEffect {
    private Action _action;

    public ChooseAndPutCardFromStackedToHandEffect(Action action, String playerId, int minimum, int maximum, Filterable stackedOn, Filterable... stackedCardsFilter) {
        super(action, playerId, minimum, maximum, stackedOn, Filters.and(stackedCardsFilter));
        _action = action;
    }

    @Override
    protected void cardsChosen(LotroGame game, Collection<PhysicalCard> stackedCards) {
        if (stackedCards.size() > 0) {
            SubCostToEffectAction subAction = new SubCostToEffectAction(_action);
            for (PhysicalCard card : stackedCards)
                subAction.appendEffect(new PutCardFromStackedIntoHandEffect(card));
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
