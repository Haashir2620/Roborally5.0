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
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a conveyor belt on the game board that moves a player to an adjacent space in the player's current heading.
 * This action is executed if there is a player on the conveyor belt and the next space in the direction the belt moves is empty.
 *
 * @return true if the player is successfully moved to the next space,
 * false otherwise (e.g., if the space is occupied or non-existent).
 * @Author Amaan Ahmad
 */

public class ConveyorBelt extends FieldAction {

    private Heading heading;


    public Heading getHeading() {
        return heading;
    }
   // heading angiver retningen af transportbåndet.

    public void setHeading(Heading heading) {
        this.heading = heading;
    }
    //Getter- og setter-metoderne giver mulighed for at læse og ændre retningen.

    /**
     * Implementation of the action of a conveyor belt. Needs to be implemented for A3.
     */
    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player == null) {
            return false; // Metoden retunerer false,hvis der ikke er nogen spiller på space, afslut tidligt
        }

        Heading heading = player.getHeading(); //  henter retningen fra spilleren
        Space space2 = gameController.board.getNeighbour(space, heading);

        if (space2 != null && space2.getPlayer() == null) { // Tjek om nabopladsen er tom
            try {
                gameController.moveToSpace(player, space2, heading); //Forsøg at flytte spilleren til nabopladsen ved hjælp af gameController.moveToSpace metoden.
                player.setSpace(space2); // Opdater spillerens placering
            } catch (ImpossibleMoveException e) {
                // Log exception eller håndter den som nødvendigt
            }
            return true; //Retunere tru hvis handligen er fuldført
        } else {
            return false; // Returner false hvis der ikke er nogen gyldig naboplads
        }
    }

}

