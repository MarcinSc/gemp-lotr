package com.gempukku.lotro.db;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DbDeckDAO implements DeckDAO {
    private DbAccess _dbAccess;
    private LotroCardBlueprintLibrary _library;

    public DbDeckDAO(DbAccess dbAccess, LotroCardBlueprintLibrary library) {
        _dbAccess = dbAccess;
        _library = library;
    }

    public synchronized LotroDeck getDeckForPlayer(Player player, String name) {
        return getPlayerDeck(player.getId(), name);
    }

    public synchronized void saveDeckForPlayer(Player player, String name, String target_format, LotroDeck deck) {
        boolean newDeck = getPlayerDeck(player.getId(), name) == null;
        storeDeckToDB(player.getId(), name, target_format, deck, newDeck);
    }

    public synchronized void deleteDeckForPlayer(Player player, String name) {
        try {
            deleteDeckFromDB(player.getId(), name);
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to delete player deck from DB", exp);
        }
    }

    public synchronized LotroDeck renameDeck(Player player, String oldName, String newName) {
        LotroDeck deck = getDeckForPlayer(player, oldName);
        if (deck == null)
            return null;
        saveDeckForPlayer(player, newName, deck.getTargetFormat(), deck);
        deleteDeckForPlayer(player, oldName);

        return deck;
    }

    public synchronized Set<String> getPlayerDeckNames(Player player) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("select name, target_format from deck where player_id=?")) {
                    statement.setInt(1, player.getId());
                    try (ResultSet rs = statement.executeQuery()) {
                        Set<String> result = new HashSet<String>();

                        while (rs.next()) {
                            String deckName = rs.getString(1);
                            String targetFormat = rs.getString(2);
                            result.add("<b>[" + targetFormat + "] - </b>" + deckName);
                        }

                        return result;
                    }
                }
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to load player decks from DB", exp);
        }
    }

    private LotroDeck getPlayerDeck(int playerId, String name) {
        try {
            try (Connection connection = _dbAccess.getDataSource().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("select contents, target_format from deck where player_id=? and name=?")) {
                    statement.setInt(1, playerId);
                    statement.setString(2, name);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next())
                            return buildDeckFromContents(name, rs.getString(1), rs.getString(2));

                        return null;
                    }
                }
            }

        } catch (SQLException exp) {
            throw new RuntimeException("Unable to load player decks from DB", exp);
        }
    }

    private void storeDeckToDB(int playerId, String name, String target_format, LotroDeck deck, boolean newDeck) {
        String contents = DeckSerialization.buildContentsFromDeck(deck);
        try {
            if (newDeck)
                storeDeckInDB(playerId, name, target_format, contents);
            else
                updateDeckInDB(playerId, name, target_format, contents);
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to store player deck to DB", exp);
        }
    }

    public synchronized LotroDeck buildDeckFromContents(String deckName, String contents, String target_format) {
        if (contents.contains("|")) {
            return DeckSerialization.buildDeckFromContents(deckName, contents, target_format);
        } else {
            // Old format
            List<String> cardsList = Arrays.asList(contents.split(","));
            String ringBearer = cardsList.get(0);
            String ring = cardsList.get(1);
            final LotroDeck lotroDeck = new LotroDeck(deckName);
            lotroDeck.setTargetFormat(target_format);
            if (ringBearer.length() > 0)
                lotroDeck.setRingBearer(ringBearer);
            if (ring.length() > 0)
                lotroDeck.setRing(ring);
            for (String blueprintId : cardsList.subList(2, cardsList.size())) {
                final LotroCardBlueprint cardBlueprint;
                try {
                    cardBlueprint = _library.getLotroCardBlueprint(blueprintId);
                    if (cardBlueprint.getCardType() == CardType.SITE)
                        lotroDeck.addSite(blueprintId);
                    else
                        lotroDeck.addCard(blueprintId);
                } catch (CardNotFoundException e) {
                    // Ignore the card
                }
            }

            return lotroDeck;
        }
    }

    private void deleteDeckFromDB(int playerId, String name) throws SQLException {
        try (Connection connection = _dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("delete from deck where player_id=? and name=?")) {
                statement.setInt(1, playerId);
                statement.setString(2, name);
                statement.execute();
            }
        }
    }

    private void storeDeckInDB(int playerId, String name, String target_format, String contents) throws SQLException {
        try (Connection connection = _dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("insert into deck (player_id, name, target_format, contents) values (?, ?, ?, ?)")) {
                statement.setInt(1, playerId);
                statement.setString(2, name);
                statement.setString(3, target_format);
                statement.setString(4, contents);
                statement.execute();
            }
        }
    }

    private void updateDeckInDB(int playerId, String name, String target_format, String contents) throws SQLException {
        try (Connection connection = _dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("update deck set contents=?, target_format=? where player_id=? and name=?")) {
                statement.setString(1, contents);
                statement.setString(2, target_format);
                statement.setInt(3, playerId);
                statement.setString(4, name);
                statement.execute();
            }
        }
    }
}
