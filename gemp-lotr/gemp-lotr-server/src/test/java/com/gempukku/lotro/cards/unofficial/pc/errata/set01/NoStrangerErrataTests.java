package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NoStrangerErrataTests
{
    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<String, String>()
                {{
                    put("aragorn", "1_89");
                    put("arwen", "1_30");
                    put("boromir", "1_97");
                    put("nostranger", "51_108");
                    put("nostranger2", "51_108");
                }}
        );
    }


    @Test
    public void NoStrangerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: *No Stranger to the Shadows
         * Side: Free Peoples
         * Culture: Gondor
         * Twilight Cost: 0
         * Type: Condition
         * Errata Game Text: At the start of each of your turns, heal up to 3 allies whose home is site 6.
         * Fellowship: Exert Galadriel to play an Elf; that Elf's twilight cost is -1.
         */

        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl nostranger = scn.GetFreepsCard("nostranger");

        assertTrue(nostranger.getBlueprint().isUnique());
        assertEquals(0, nostranger.getBlueprint().getTwilightCost());
        assertTrue(scn.HasKeyword(nostranger, Keyword.STEALTH));
    }

    @Test
    public void NoStrangerCanOnlyPlayOnRanger() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
        PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
        PhysicalCardImpl nostranger = scn.GetFreepsCard("nostranger");

        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCharToTable(arwen);
        scn.FreepsMoveCharToTable(boromir);
        scn.FreepsMoveCardToHand(nostranger);

        scn.StartGame();

        assertTrue(scn.FreepsCardPlayAvailable(nostranger));

        scn.FreepsPlayCard(nostranger);

        //There are 3 companions in play, but only 2 rangers, so we should only see 2 options
        assertEquals(2, scn.FreepsGetADParamAsList("cardId").size());
    }


    @Test
    public void NoStrangerReducesTwilight() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        PhysicalCardImpl nostranger = scn.GetFreepsCard("nostranger");

        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(nostranger);

        scn.StartGame();

        scn.FreepsPlayCard(nostranger);

        scn.FreepsSkipCurrentPhaseAction();

        // 2 for Frodo/Aragorn, 1 for the site, -1 for No Stranger
        assertEquals(2, scn.GetTwilight());
    }
}
