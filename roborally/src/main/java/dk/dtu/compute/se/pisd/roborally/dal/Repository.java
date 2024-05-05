/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.dal;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * the reporsitory to change the database
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Mohammad Haashir Khan, Amaan Ahmad
 */
class Repository implements IRepository {

	private static final String GAME_GAMEID = "gameID";

	private static final String GAME_NAME = "name";

	private static final String GAME_CURRENTPLAYER = "currentPlayer";

	private static final String GAME_PHASE = "phase";

	private static final String GAME_STEP = "step";

	private static final String PLAYER_PLAYERID = "playerID";

	private static final String PLAYER_NAME = "name";

	private static final String PLAYER_COLOUR = "colour";

	private static final String PLAYER_GAMEID = "gameID";

	private static final String PLAYER_POSITION_X = "positionX";

	private static final String PLAYER_POSITION_Y = "positionY";

	private static final String PLAYER_CHECKPOINTVALU = "checkpointvalue";

	private static final String PLAYER_HP = "hp";

	private static final String PLAYER_HEADING = "heading";
	private static final String FIELD_GAMEID = "gameID";
	private static final String FIELD_PLAYERID = "playerID";
	private static final String FIELD_TYPE = "type";
	private static final int FIELD_TYPE_REGISTER = 0;
	private static final int FIELD_TYPE_HAND = 1;
	private static final String FIELD_POS = "position";
	private static final String FIELD_VISIBLE = "visible";
	private static final String FIELD_COMMAND = "command";

	private Connector connector;

	Repository(Connector connector) {
		this.connector = connector;
	}

	/**
	 * Attempts to create a new game record in the database along with associated player and card field records.
	 * This method initializes a transaction to handle the creation of game, player, and card entries
	 * atomically, ensuring that all database operations either complete successfully or rollback entirely.
	 * If the game already has an assigned ID, it will not proceed with the database insertion.
	 *
	 * @param game The game board object containing state information to be stored.
	 * @param k An integer representing additional game data, such as the selected board configuration.
	 * @return true if the game was successfully created in the database; false otherwise.
	 * @throws SQLException if any database operations fail during execution.
	 *
	 * @author Mohamamd Haashir khan, Amaan Ahmed
	 *
	 */

	@Override
	public boolean createGameInDB(Board game, int k) {

		if (game.getGameId() == null) {

			Connection connection = connector.getConnection();
			try {
				connection.setAutoCommit(false);

				PreparedStatement ps = getInsertGameStatementRGK();

				ps.setString(1, "Date: " + new Date());
				ps.setNull(2, Types.TINYINT);
				ps.setInt(3, game.getPhase().ordinal());
				ps.setInt(4, game.getStep());
				ps.setInt(5, k);

				// If you have a foreign key constraint for current players,
				// the check would need to be temporarily disabled, since
				// MySQL does not have a per transaction validation, but
				// validates on a per row basis.
				Statement statement = connection.createStatement();
				statement.execute("SET foreign_key_checks = 0");

				int affectedRows = ps.executeUpdate();
				ResultSet generatedKeys = ps.getGeneratedKeys();
				if (affectedRows == 1 && generatedKeys.next()) {
					game.setGameId(generatedKeys.getInt(1));
				}
				generatedKeys.close();

				// Enable foreign key constraint check again:
				statement.execute("SET foreign_key_checks = 1");
				statement.close();

				createPlayersInDB(game);
				//TOODO this method needs to be implemented first
				//System.out.println("hej med dig");
				createCardFieldsInDB(game);

				ps = getSelectGameStatementU();
				ps.setInt(1, game.getGameId());

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNumber(game.getCurrentPlayer()));
					rs.updateRow();
				} else {

				}
				rs.close();

				connection.commit();
				connection.setAutoCommit(true);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("Some DB error");

				try {
					connection.rollback();
					connection.setAutoCommit(true);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			System.err.println("Game cannot be created in DB, since it has a game id already!");
		}
		return false;
	}

	/**
	 * Updates the existing game state in the database including related player and card field entries.
	 * This method handles the update within a transaction to ensure that all modifications are either
	 * fully applied or completely rolled back. It first updates the game's basic details such as the
	 * current player, phase, and step, then proceeds to update player and card field data.
	 *
	 * @param game The game object containing updated state information.
	 * @return true if the database update operation is successful, false if it fails or if the game ID is null.
	 * @throws SQLException if any issues occur during database access or update operations.,
	 *
	 * @author Mohammad Haashir Khan, Amaan Ahmed.
	 */

	@Override
	public boolean updateGameInDB(Board game) {
		assert game.getGameId() != null;

		Connection connection = connector.getConnection();
		try {
			connection.setAutoCommit(false);

			PreparedStatement ps = getSelectGameStatementU();
			ps.setInt(1, game.getGameId());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNumber(game.getCurrentPlayer()));
				rs.updateInt(GAME_PHASE, game.getPhase().ordinal());
				rs.updateInt(GAME_STEP, game.getStep());
				rs.updateRow();
			} else {
			}
			rs.close();

			updatePlayersInDB(game);
			//TOODO this method needs to be implemented first
			updateCardFieldsInDB(game);

			connection.commit();
			connection.setAutoCommit(true);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Some DB error");

			try {
				connection.rollback();
				connection.setAutoCommit(true);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * Loads a game from the database using the specified game ID. This method retrieves the game's data,
	 * including board configuration, current player, game phase, and steps from the database and reconstructs
	 * the game state. It also loads associated players and card fields to fully restore the game context.
	 *
	 * @param id The unique identifier of the game to load from the database.
	 * @return The loaded Board object fully populated with its corresponding state, or null if no valid game is found or an error occurs.
	 * @throws SQLException if any database access errors occur during the process.
	 *
	 * @author Mohammad Haashir Khan, Amaan Ahmed.
	 */

	@Override
	public Board loadGameFromDB(int id) {
		Board game;
		try {
			PreparedStatement ps = getSelectGameStatementU();
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();
			int playerNo = -1;
			if (rs.next()) {

				game = LoadBoard.loadBoard(rs.getInt("boardname"));
				if (game == null) {
					return null;
				}
				playerNo = rs.getInt(GAME_CURRENTPLAYER);
				game.setPhase(Phase.values()[rs.getInt(GAME_PHASE)]);
				game.setStep(rs.getInt(GAME_STEP));
			} else {
				return null;
			}
			rs.close();

			game.setGameId(id);
			loadPlayersFromDB(game);

			if (playerNo >= 0 && playerNo < game.getPlayersNumber()) {
				game.setCurrentPlayer(game.getPlayer(playerNo));
			} else {
				return null;
			}

			//TOODO this method needs to be implemented first
			loadCardFieldsFromDB(game);


			return game;
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Some DB error");
		}
		return null;
	}

	/**
	 * This is a method, that is used for choosing games from the database.
	 *
	 * @return the result that is list of all the games.
	 */
	@Override
	public List<GameInDB> getGames() {
		List<GameInDB> result = new ArrayList<>();
		try {
			PreparedStatement ps = getSelectGameIdsStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(GAME_GAMEID);
				String name = rs.getString(GAME_NAME);
				result.add(new GameInDB(id, name));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Creates player records in the database for each player in the game. This method iterates through each player
	 * in the provided game object, inserting their current state into the database. This includes the player's
	 * name, color, position, heading, checkpoint value, and health points.
	 *
	 * @param game The game object containing the players to be stored in the database.
	 * @throws SQLException If there is an error executing the database commands, such as a connection issue
	 *                      or a syntax error in the SQL statement.
	 *
	 * @author Mohamamd Haashir Khan
	 */

	private void createPlayersInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectPlayersStatementU();
		ps.setInt(1, game.getGameId());

		ResultSet rs = ps.executeQuery();
		for (int i = 0; i < game.getPlayersNumber(); i++) {
			Player player = game.getPlayer(i);
			rs.moveToInsertRow();
			rs.updateInt(PLAYER_GAMEID, game.getGameId());
			rs.updateInt(PLAYER_PLAYERID, i);
			rs.updateString(PLAYER_NAME, player.getName());
			rs.updateString(PLAYER_COLOUR, player.getColor());
			rs.updateInt(PLAYER_POSITION_X, player.getSpace().x);
			rs.updateInt(PLAYER_POSITION_Y, player.getSpace().y);
			rs.updateInt(PLAYER_HEADING, player.getHeading().ordinal());
			rs.updateInt(PLAYER_CHECKPOINTVALU, player.getCheckpointValue());
			rs.updateInt(PLAYER_HP, player.getHp());
			rs.insertRow();
		}

		rs.close();
	}
	/**
	 * Loads player data from the database into the specified game object. This method retrieves all players
	 * associated with a given game ID and initializes them within the game. It sets each player's attributes such as
	 * name, color, position, heading, checkpoint value, and health points based on the stored data.
	 *
	 * @param game The game object where the players will be loaded into.
	 * @throws SQLException If there is a failure in database communication or query execution,
	 *                      potentially leading to incomplete player data retrieval.
	 *
	 * @author Mohammad Haashir Khan
	 */

	private void loadPlayersFromDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectPlayersASCStatement();
		ps.setInt(1, game.getGameId());

		ResultSet rs = ps.executeQuery();
		int i = 0;
		while (rs.next()) {
			int playerId = rs.getInt(PLAYER_PLAYERID);
			if (i++ == playerId) {
				String name = rs.getString(PLAYER_NAME);
				String colour = rs.getString(PLAYER_COLOUR);
				Player player = new Player(game, colour, name);
				game.addPlayer(player);

				int x = rs.getInt(PLAYER_POSITION_X);
				int y = rs.getInt(PLAYER_POSITION_Y);
				player.setSpace(game.getSpace(x, y));
				int heading = rs.getInt(PLAYER_HEADING);
				player.setHeading(Heading.values()[heading]);
				int chekpointvaule = rs.getInt(PLAYER_CHECKPOINTVALU);
				player.setCheckpointValue(chekpointvaule);
				int hp = rs.getInt(PLAYER_HP);
				player.setHp(hp);
			} else {
				System.err.println("Game in DB does not have a player with id " + i + "!");
			}
		}
		rs.close();
	}

	/**
	 * Updates the player details in the database for a specific game. This method synchronizes the current
	 * state of players in the game object with the database records. It updates the players' positions, headings,
	 * checkpoint values, and health points based on their current state in the game.
	 *
	 * @param game The game object whose player data is to be updated in the database.
	 * @throws SQLException If any SQL error occurs during the update process, ensuring the method caller
	 *                      can handle such exceptions appropriately.
	 *
	 * @author Mohamamd Haashir Khan
	 */

	private void updatePlayersInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectPlayersStatementU();
		ps.setInt(1, game.getGameId());

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int playerId = rs.getInt(PLAYER_PLAYERID);
			Player player = game.getPlayer(playerId);
			rs.updateInt(PLAYER_POSITION_X, player.getSpace().x);
			rs.updateInt(PLAYER_POSITION_Y, player.getSpace().y);
			rs.updateInt(PLAYER_HEADING, player.getHeading().ordinal());
			rs.updateInt(PLAYER_CHECKPOINTVALU, player.getCheckpointValue());
			rs.updateInt(PLAYER_HP, player.getHp());
			rs.updateRow();
		}
		rs.close();
	}

	//-----------------------------------------------------------------------------------------------------------------

	/**
	 * Populates the database with card field data for each player in a game. This method iterates through
	 * each player and their program and hand card fields to store the visibility, type, and command of each card.
	 * It creates new rows for each card field in the database and sets the corresponding attributes.
	 *
	 * @param game The game object containing players whose card field data needs to be stored.
	 * @throws SQLException If any SQL error occurs during the process, allowing for appropriate exception handling.
	 *
	 * @author Mohamamd Haashir Khan, Amaan Ahmed
	 */

	private void createCardFieldsInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardFieldStatementU();
		ps.setInt(1, game.getGameId());

		ResultSet rs = ps.executeQuery();

		for (int i = 0; i < game.getPlayersNumber(); i++) {
			Player player = game.getPlayer(i);
			for (int j = 0; j < 5; j++) {
				rs.moveToInsertRow();
				rs.updateInt(FIELD_GAMEID, game.getGameId());
				rs.updateInt(FIELD_PLAYERID, i);
				rs.updateInt(FIELD_TYPE, 0);
				rs.updateInt(FIELD_POS, j);
				rs.updateBoolean(FIELD_VISIBLE, player.getProgramField(j).isVisible());
				CommandCard cards = player.getProgramField(j).getCard();
				if (cards != null) {
					Command card1 = player.getProgramField(j).getCard().command;
					Integer card2 = card1.ordinal();
					if (card2 != null) {
						rs.updateInt(FIELD_COMMAND, card1.ordinal());
					} else {
						rs.updateNull(FIELD_COMMAND);
					}
				}
				rs.insertRow();
			}
			for (int j = 0; j < 8; j++) {
				rs.moveToInsertRow();
				rs.updateInt(FIELD_GAMEID, game.getGameId());
				rs.updateInt(FIELD_PLAYERID, i);
				rs.updateInt(FIELD_TYPE, 1);
				rs.updateInt(FIELD_POS, j);
				rs.updateBoolean(FIELD_VISIBLE, player.getCardField(j).isVisible());
				CommandCard cards = player.getCardField(j).getCard();
				if (cards != null) {
					Command card3 = player.getCardField(j).getCard().command;
					Integer card4 = card3.ordinal();
					if (card4 != null) {
						rs.updateInt(FIELD_COMMAND, card3.ordinal());
					} else {
						rs.updateNull(FIELD_COMMAND);
					}
				}
				rs.insertRow();
			}
		}
	}
//---------------------------------------------------------------------------------------------------------------------
	/**
	 * Loads card field data from the database for each player in a specified game. It retrieves the
	 * visibility, position, and associated command for each card field stored in the database and
	 * updates the corresponding objects in the game model.
	 *
	 * @param game The game object whose players' card fields are being populated.
	 * @throws SQLException If any SQL issues occur while retrieving data.
	 *
	 * @author Mohammmad Haashir khan, Amaan Ahmed
	 */

	private void loadCardFieldsFromDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardFieldStatement();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int playerID = rs.getInt(FIELD_PLAYERID);
			Player player = game.getPlayer(playerID);
			int type = rs.getInt(FIELD_TYPE);
			int pos = rs.getInt(FIELD_POS);
			CommandCardField field;
			if (type == FIELD_TYPE_REGISTER) {
				field = player.getProgramField(pos);
			} else if (type == FIELD_TYPE_HAND) {
				field = player.getCardField(pos);
			} else {
				field = null;
			}
			if (field != null) {
				field.setVisible(rs.getBoolean(FIELD_VISIBLE));
				Object c = rs.getObject(FIELD_COMMAND);
				if (c != null) {
					Command card = Command.values()[rs.getInt(FIELD_COMMAND)];
					field.setCard(new CommandCard(card));
				}
			}
		}
		rs.close();
	}

	//------------------------------------------------------------------------------------------------------------------
	/**
	 * Updates the database records for card fields associated with each player in a specified game.
	 * This method synchronizes the visibility status and command of each card field in the game model with the database.
	 * It processes each card field, updating its stored properties or setting them to null if no command is assigned.
	 *
	 * @param game The game object whose card fields are being updated in the database.
	 * @throws SQLException If any SQL issues occur during the update process, ensuring robust data handling.
	 *
	 * @author Mohammmad Haashir khan, Amaan Ahmed
	 */

	private void updateCardFieldsInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardFieldStatementU();
		ps.setInt(1, game.getGameId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int playerId = rs.getInt(FIELD_PLAYERID);
			Player player = game.getPlayer(playerId);
			int type = rs.getInt(FIELD_TYPE);
			int pos = rs.getInt(FIELD_POS);
			CommandCardField field = null;
			if (FIELD_TYPE_REGISTER == type) {
				field = player.getProgramField(pos);
			} else if (FIELD_TYPE_HAND == type) {
				field = player.getCardField(pos);
			}

			if (field != null) {
				rs.updateBoolean(FIELD_VISIBLE, field.isVisible());
				CommandCard card = field.getCard();
				if (card != null) {
					rs.updateInt(FIELD_COMMAND, card.command.ordinal());
				} else {
					rs.updateNull(FIELD_COMMAND);
				}
			}
			rs.updateRow();
		}
		rs.close();
	}

	//------------------------------------------------------------------------------------------------------------------


	private static final String SQL_INSERT_GAME =
			"INSERT INTO Game(name, currentPlayer, phase, step, boardname) VALUES (?, ?, ?, ?, ?)";

	private PreparedStatement insert_game_stmt = null;

	private PreparedStatement getInsertGameStatementRGK() {
		if (insert_game_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				insert_game_stmt = connection.prepareStatement(
						SQL_INSERT_GAME,
						Statement.RETURN_GENERATED_KEYS);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return insert_game_stmt;
	}

	private static final String SQL_SELECT_GAME =
			"SELECT * FROM Game WHERE gameID = ?";

	private PreparedStatement select_game_stmt = null;

	private PreparedStatement getSelectGameStatementU() {
		if (select_game_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_game_stmt = connection.prepareStatement(
						SQL_SELECT_GAME,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return select_game_stmt;
	}

	private static final String SQL_SELECT_PLAYERS =
			"SELECT * FROM Player WHERE gameID = ?";

	private PreparedStatement select_players_stmt = null;

	private PreparedStatement getSelectPlayersStatementU() {
		if (select_players_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_players_stmt = connection.prepareStatement(
						SQL_SELECT_PLAYERS,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return select_players_stmt;
	}

	private static final String SQL_SELECT_CARD_FIELDS = "SELECT * FROM Cardfield WHERE gameID = ?";

	private PreparedStatement select_card_field_stmt = null;

	private PreparedStatement getSelectCardFieldStatement() {
		if (select_card_field_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_card_field_stmt = connection.prepareStatement(
						SQL_SELECT_CARD_FIELDS,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return select_card_field_stmt;
	}

	private PreparedStatement select_card_field_stmt_u = null;

	private PreparedStatement getSelectCardFieldStatementU() {
		if (select_card_field_stmt_u == null) {
			Connection connection = connector.getConnection();
			try {
				select_card_field_stmt_u = connection.prepareStatement(
						SQL_SELECT_CARD_FIELDS,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return select_card_field_stmt_u;
	}

	private static final String SQL_SELECT_PLAYERS_ASC =
			"SELECT * FROM Player WHERE gameID = ? ORDER BY playerID ASC";
	private PreparedStatement select_players_asc_stmt = null;

	private PreparedStatement getSelectPlayersASCStatement() {
		if (select_players_asc_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				// This statement does not need to be updatable
				select_players_asc_stmt = connection.prepareStatement(
						SQL_SELECT_PLAYERS_ASC);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return select_players_asc_stmt;
	}

	private PreparedStatement select_Card_field_asc_stmt = null;

	private static final String SQL_SELECT_GAMES =
			"SELECT gameID, name FROM Game";

	private PreparedStatement select_games_stmt = null;

	private PreparedStatement getSelectGameIdsStatement() {
		if (select_games_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_games_stmt = connection.prepareStatement(
						SQL_SELECT_GAMES);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return select_games_stmt;
	}


}
