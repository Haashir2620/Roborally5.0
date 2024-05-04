package dk.dtu.compute.se.pisd.roborally.controller;


import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;
/**
 * Executes the action of falling into a pit on a specified space. If a player is present, it reduces their health by one.
 * If health falls to zero, the player is reset to a starting health and checkpoints. The player is then moved to a random
 * unoccupied space on the board. This method ensures game continuity by managing player penalties and repositioning.
 * @author Ali Hassan, Asim Raja, Muhammed Feyez
 */

public class Pit extends FieldAction {
    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player player = space.getPlayer();
        //return false, hvis player null
        if (player == null) {
            return false;
        }
        //logik for hvad der sker når spiller falder i pit, og når spilleren mister liv
        player.setHp(player.getHp() - 1);
        Space randomSpace = gameController.board.getRandomSpace();
        while (randomSpace.getPlayer() != null && randomSpace != space) {
            randomSpace = gameController.board.getRandomSpace();
        }
        if (player.getHp() <= 0) {
            player.setHp(3);
            player.setCheckpointValue(0);
        }
        return true;
    }
}
