package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/**
 * This test clas is created to test Pit, which is done in the doActions method
 * @author Amaan Ahmed
 */

class PitTest {

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
    void PitAction() {
        Board board = gameController.board;
        Player player1 = board.getCurrentPlayer();
        //We know that there is a Pit on space x = 4 y = 7
        player1.setSpace(board.getSpace(4, 7));
        int hp = player1.getHp();
        gameController.performFieldActions();
        Assertions.assertEquals(player1.getHp(), hp -1, "the old hp - 1 ");

    }
}