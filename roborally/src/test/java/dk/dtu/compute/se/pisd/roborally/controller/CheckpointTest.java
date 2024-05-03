package dk.dtu.compute.se.pisd.roborally.controller;

import static org.junit.jupiter.api.Assertions.*;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CheckpointTest {


    private GameController gameController;

    @BeforeEach
    void setUp() {
        Board board = LoadBoard.loadBoard(1);
        gameController = new GameController(board);
        for (int i = 0; i < 6; i++) {
            Player player1 = new Player(board, null, "Player1 " + i);
            board.addPlayer(player1);
            player1.setSpace(board.getSpace(i, i));
            player1.setHeading(Heading.values()[i % Heading.values().length]);
        }
        board.setCurrentPlayer(board.getPlayer(0));

    }


    @AfterEach
    void tearDown() {
        gameController = null;
    }


    @Test
    void updateCheckpoint() {
        Board board = gameController.board;
        Player player1 = board.getCurrentPlayer();
        player1.setCheckpointValue(4);
        player1.setSpace(board.getSpace(0,2));
        int CheckpointValue = player1.getCheckpointValue();
        gameController.performFieldActions();
        Assertions.assertEquals(CheckpointValue + 1, player1.getCheckpointValue());


    }

    @Test
    void checkpointChecker() {
        Board board = gameController.board;
        Player player1 = board.getCurrentPlayer();
        player1.setCheckpointValue(4);
        int NewCheckpointValue = player1.getCheckpointValue();
        player1.setSpace(board.getSpace(0,2));
        gameController.performFieldActions();
        Assertions.assertEquals(player1.getCheckpointValue(), NewCheckpointValue + 1, "Player have skipped a checkpoint");



    }
}
