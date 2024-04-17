package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Phase;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

/**
 * Checkpoint class, is the class for every checkpoint in the game.
 *
 *
 */
public class Checkpoint extends FieldAction {
    private int checkpointnumber;

    public Checkpoint(int checkpointnumber) {
        this.checkpointnumber = checkpointnumber;
    }

    public int getCheckpointnumber() {
        return checkpointnumber;
    }

    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player player = space.getPlayer();
        if (player != null && player.getCheckpointValue() + 1 == checkpointnumber) {
            player.setCheckpointValue(checkpointnumber);
            if (checkpointnumber == 5) {
                gameController.board.setPhase(Phase.GAME_ENDING);
            }
            return true;
        }
        return false;
    }
}
