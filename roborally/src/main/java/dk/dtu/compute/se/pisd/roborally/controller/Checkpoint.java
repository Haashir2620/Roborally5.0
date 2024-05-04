package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Phase;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

/**
 * Represents a checkpoint action on a game board space. When a player reaches this checkpoint,
 * it updates their checkpoint progress if the checkpoint number matches their next expected checkpoint.
 * If the player reaches the final checkpoint (number 6), it triggers the end of the game.
 *
 * @return true if the player successfully hits the checkpoint, false if not or no player is present.
 * @author Mohammad Haashir Khan
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
            gameController.board.getStatusMessage();
            if (checkpointnumber == 1) {
                gameController.board.setPhase(Phase.GAME_ENDING);
            }
            return true;
        }
        return false;
    }
}
