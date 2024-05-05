package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;

import dk.dtu.compute.se.pisd.roborally.RoboRally;

import dk.dtu.compute.se.pisd.roborally.dal.GameInDB;
import dk.dtu.compute.se.pisd.roborally.dal.IRepository;
import dk.dtu.compute.se.pisd.roborally.dal.RepositoryAccess;
import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.*;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * this the class that starts the game. makes everything ready for a game.
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class
AppController implements Observer {

    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);
    final private List<Integer> BOARD_OPTIONS = Arrays.asList(1, 2);
    final private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");

    final private RoboRally roboRally;

    private GameController gameController;
    private Board board;


    IRepository repository = RepositoryAccess.getRepository();


    public AppController(@NotNull RoboRally roboRally) {
        this.roboRally = roboRally;
    }

    /**
     * The newGame() method purpose is to start a new game, and you can choose the number of playerd
     */
    public void newGame() {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(PLAYER_NUMBER_OPTIONS.get(0), PLAYER_NUMBER_OPTIONS);
        dialog.setTitle("Player number");
        dialog.setHeaderText("Select number of players");
        Optional<Integer> result = dialog.showAndWait();


        /**
         /**
         * Initiates the process to start a new game, allowing users to select the number of players and the game board.
         * If an existing game is in progress, it offers the option to save or abort the current game.
         * A new game environment is set up with the specified board and players, and the game state is saved to the database.
         * Finally, the game view is updated to reflect the new game setup.
         * @author Asim Raja, Muhammad Feyez, Ali Hassan
         *
         * @param result The optional integer from the player number choice dialog.
         * @param result1 The optional integer from the board selection choice dialog.
         */



        ChoiceDialog<Integer> dialog1 = new ChoiceDialog<>(BOARD_OPTIONS.get(0), BOARD_OPTIONS);
        dialog1.setTitle("Board number");
        dialog1.setHeaderText("Select Board");
        Optional<Integer> result1 = dialog1.showAndWait();


        if (result.isPresent()) {
            if (gameController != null) {
                // The UI should not allow this, but in case this happens anyway.
                // give the user the option to save the game or abort this operation!
                if (!stopGame()) {
                    return;
                }
            }

            // XXX the board should eventually be created programmatically or loaded from a file
            //     here we just create an empty board with the required number of players.
            //Board board = Boards.createBoard("Board1");
            Board board = LoadBoard.loadBoard(result1.get());

            gameController = new GameController(board);
            int no = result.get();
            for (int i = 0; i < no; i++) {
                Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
                player.setHp(3);
                board.addPlayer(player);
                player.setSpace(board.getSpace(i % board.width, i));

            }

            gameController.startProgrammingPhase();
            repository.createGameInDB(gameController.board, result1.get());
            roboRally.createBoardView(gameController);
        }
    }

    /**
     * saveGame() method is used to save the game in the database, and using updateGameinDB()
     * to update the game in the database
     */
    public void saveGame() {
        repository.updateGameInDB(gameController.board);
    }


    /**
     * /**
     * Loads a game from the database based on user selection. The method retrieves a list of saved games,
     * displays them in a reversed order for user selection, and loads the selected game. It sets up the game
     * environment by initializing a new GameController with the loaded board, reassigns the players to their
     * saved positions, and updates the game view to reflect the current game state.
     *
     * @author Mohammmad Haashir khan, Amaan Ahmed
     *  */

    public void loadGame() {
        List<GameInDB> list = repository.getGames();
        Collections.reverse(list);
        ChoiceDialog<GameInDB> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle("");
        dialog.setHeaderText("Choose game to load");
        Optional<GameInDB> result = dialog.showAndWait();
        if (result.isPresent()) {
            int id = result.get().id;
            Board board = repository.loadGameFromDB(id);
            gameController = new GameController(board);
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                board.addPlayer(player);
                player.setSpace(board.getPlayer(i).getSpace());
            }
            roboRally.createBoardView(gameController);
        }
    }
    /**
     * Attempts to stop the current game. If a game is active, it saves the game automatically,
     * clears the current game controller, updates the view to reflect no active game, and returns true.
     * If no game is active, it returns false, indicating that there was no game to stop.
     *
     * @return true if a game was active and has been stopped, false otherwise.
     */

    public boolean stopGame() {
        if (gameController != null) {

            // here we save the game (without asking the user).
            saveGame();

            gameController = null;
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }

    /**
     * Exits the Roborally application. If a game is active, it prompts the user to confirm the exit action
     */
    public void exit() {
        if (gameController != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Exit RoboRally?");
            alert.setContentText("Are you sure you want to exit RoboRally?");
            Optional<ButtonType> result = alert.showAndWait();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return; // return without exiting the application
            }
        }
        // if there is no game or the user confirmed the exit, we exit the application
        if (gameController == null || stopGame()) {
            Platform.exit();
        }
    }

    public boolean isGameRunning() {
        return gameController != null;
    }


    @Override
    public void update(Subject subject) {
        // XXX do nothing for now
    }
}

