package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_285_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "1_285");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void BilbosPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Bilbo's Pipe
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Shire
		* Twilight Cost: 1
		* Type: possession
		* Subtype: Pipe
		* Game Text: Bearer must be a Hobbit.<br><b>Fellowship:</b> Discard a pipeweed possession and spot X pipes to shuffle X tales from your discard pile into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Bilbo's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BilbosPipeTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.FreepsMoveCardToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
