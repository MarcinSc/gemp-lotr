package com.gempukku.lotro.cards.set3.site;

import com.gempukku.lotro.cards.AbstractSite;
import com.gempukku.lotro.common.Block;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.modifiers.AbstractModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ModifierEffect;
import com.gempukku.lotro.logic.modifiers.ModifiersQuerying;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collections;
import java.util.List;

/**
 * Set: Realms of Elf-lords
 * Twilight Cost: 6
 * Type: Site
 * Site: 7
 * Game Text: River. While the fellowship is at The Great River, cards may not be played from draw decks or discard piles.
 */
public class Card3_118 extends AbstractSite {
    public Card3_118() {
        // TODO Check direction of the site
        super("The Great River", Block.FELLOWSHIP, 7, 6, null);
        addKeyword(Keyword.RIVER);
    }

    @Override
    public List<? extends Modifier> getAlwaysOnModifiers(PhysicalCard self) {
        return Collections.singletonList(
                new AbstractModifier(self, "Cards may not be played from draw decks or discard piles", null, new ModifierEffect[]{ModifierEffect.ACTION_MODIFIER}) {
                    @Override
                    public boolean canPlayAction(GameState gameState, ModifiersQuerying modifiersQuerying, Action action, boolean result) {
                        PhysicalCard source = action.getActionSource();
                        if (source != null && (source.getZone() == Zone.DECK || source.getZone() == Zone.DISCARD))
                            return false;
                        return result;
                    }
                });
    }
}
