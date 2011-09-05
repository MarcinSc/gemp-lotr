package com.gempukku.lotro.game;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.List;

public interface LotroCardBlueprint {
    public Side getSide();

    public CardType getCardType();

    public Culture getCulture();

    public boolean isUnique();

    public String getName();

    public Signet getSignet();

    public boolean hasKeyword(Keyword keyword);

    public int getKeywordCount(Keyword keyword);

    public int getTwilightCost();

    public int getStrength();

    public int getVitality();

    public int getResistance();

    public Modifier getAlwaysOnEffect(PhysicalCard self);

    public List<? extends Action> getInPlayPhaseActions(String playerId, LotroGame lotroGame, PhysicalCard self);

    public List<? extends Action> getRequiredBeforeTriggers(LotroGame lotroGame, Effect effect, EffectResult effectResult, PhysicalCard self);

    public List<? extends Action> getOptionalBeforeTriggers(String playerId, LotroGame lotroGame, Effect effect, EffectResult effectResult, PhysicalCard self);

    public List<? extends Action> getOptionalBeforeActions(String playerId, LotroGame lotroGame, Effect effect, EffectResult effectResult, PhysicalCard self);

    public List<? extends Action> getRequiredAfterTriggers(LotroGame lotroGame, EffectResult effectResult, PhysicalCard self);

    public List<? extends Action> getOptionalAfterTriggers(String playerId, LotroGame lotroGame, EffectResult effectResult, PhysicalCard self);

    public List<? extends Action> getOptionalAfterActions(String playerId, LotroGame lotroGame, EffectResult effectResult, PhysicalCard self);

    public int getSiteNumber();

    public Direction getSiteDirection();

    public enum Direction {
        LEFT, RIGHT
    }
}
