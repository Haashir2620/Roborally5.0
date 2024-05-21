package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.ImpossibleMoveException;
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
 *package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.model.*;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class GameController {

    final public Board board;
    

    public GameController(@NotNull Board board) {
        this.board = board;
    }

    /**
     * This is just some dummy controller operation to make a simple move to see something
     * happening on the board. This method should eventually be deleted!
     *
     * @param space the space to which the current player should move
     *
     */

    public void moveCurrentPlayerToSpace(@NotNull Space space) {
        if (space.getPlayer() == null) {
            Player currentPlayer = board.getCurrentPlayer();
            space.setPlayer(currentPlayer); // Tildeler currentPlayer til dette space
            board.setMoveCounter(board.getMoveCounter() + 1); // Opdaterer moveCounter med én
            board.getStatusMessage(); // Henter den opdaterede statusbesked, men bruger den ikke

            // Beregner indeks for den næste spiller og opdaterer currentPlayer
            int currentPlayerIndex = board.getPlayerNumber(currentPlayer);
            int totalPlayers = board.getPlayersNumber();
            if (currentPlayerIndex == totalPlayers - 1) {
                // Hvis nuværende spiller er den sidste, cykler tilbage til den første spiller
                currentPlayer = board.getPlayer(0);
            } else {
                // Ellers, gå til den næste spiller
                currentPlayer = board.getPlayer(currentPlayerIndex + 1);
            }
            board.setCurrentPlayer(currentPlayer);
        }
    }

        /**
         * this method starts the programmingphase
         */
    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    CommandCardField field = player.getCardField(j);
                    field.setCard(  generateRandomCommandCard());
                    field.setVisible(true);
                }
            }
        }
    }

    /**
     * generates random card
     * @return commandcard
     */
    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    /**
     * This method finishes the programmingphase
     */
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    /**
     *This method makes the programfield visible
     */
    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    /**
     * This method makes the programfield unvisible
     */
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    /**
     * This method executes the programs
     */
    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    /**
     * This method executes the step
     */
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    /**
     * This method continues the program
     */
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    /**
     * Handles player interaction during the game's interactive command phase, executing a chosen command option.
     * After executing the command, it updates the game phase, advances the game to the next player, and, if all players have acted,
     * progresses to the next step or restarts the programming phase.
     */

    private void executeNextStep() {

        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {

            int step = board.getStep();

            if (step >= 0 && step < Player.NO_REGISTERS) {

                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null) {
                    Command command = card.command;
                    if (command.isInteractive()) {
                        board.setPhase(Phase.PLAYER_INTERACTION);
                        return;
                    }
                    executeCommand(currentPlayer, command);
                }
                int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()) {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                } else {
                    //--> execute actions on fields!
                    //--> check checkpoints for alle spillere
                    performFieldActions();
                    String status =  board.getStatusMessage();

                    System.out.println(status);
                    step++;
                    System.out.println(status);


                    if (step < Player.NO_REGISTERS) {
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    } else {
                        startProgrammingPhase();
                    }

                }
            } else {
                // this should not happen
                assert false;
            }

        } else {
            // this should not happen
            assert false;
        }
    }

    /**
     * Handles player interaction during the game's interactive command phase, executing a chosen command option.
     * After executing the command, it updates the game phase, advances the game to the next player, and, if all players have acted,
     * progresses to the next step or restarts the programming phase.
     */

    public void executeCommandOptionAndContinue(@NotNull Command option) {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.PLAYER_INTERACTION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null) {
                    Command command = card.command;
                    executeCommand(currentPlayer, option);
                    if (command.isInteractive()) {
                        board.setPhase(Phase.ACTIVATION);
                        //return;
                    }
                    int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                    if (nextPlayerNumber < board.getPlayersNumber()) {
                        board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                    } else {
                        step++;
                        if (step < Player.NO_REGISTERS) {
                            makeProgramFieldsVisible(step);
                            board.setStep(step);
                            board.setCurrentPlayer(board.getPlayer(0));
                        } else {
                            startProgrammingPhase();
                        }
                    }
                }
            } else {
                // this should not happen
                assert false;
            }
        } else {
            // this should not happen
            assert false;
        }

        if (!board.isStepMode() && board.getPhase() == Phase.ACTIVATION) {
            continuePrograms();
        }
    }


    /**
     * execute a commandcard depending on what it is
     * @param player is the player that executes the command
     * @param command is the commandcard that player executes
     *
     */
    private void executeCommand(@NotNull Player player, Command command) {
        if (player != null && player.board == board && command != null) {
            // XXX This is a very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a more elegant way
            //     (this concerns the way cards are modelled as well as the way they are executed).

            switch (command) {
                case FORWARD:
                    this.moveForward(player);
                    break;
                case RIGHT:
                    this.turnRight(player);
                    break;
                case LEFT:
                    this.turnLeft(player);
                    break;
                case FAST_FORWARD:
                    this.fastForward(player);
                    break;
                case Move2:
                    this.move2(player);
                    break;
                case Uturn:
                    this.uTurn(player);
                    break;
                case Moveback:
                    this.turnRight(player);
                    this.turnRight(player);
                    this.moveForward(player);
                    this.turnRight(player);
                    this.turnRight(player);
                    break;
                default:
                    // DO NOTHING (for now)
            }
        }
    }

    /**
     * Moves a player forward in the direction they are currently facing. If the target space is available,
     * the player is moved to that space.
     * An ImpossibleMoveException is caught and handled silently if the move cannot be completed.
     * @author Mohammad Haashir Khan
     */
    public void moveForward(@NotNull Player player) {
        if (player.board == board) {
            Space space = player.getSpace();
            Heading heading = player.getHeading();
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                try {
                    moveToSpace(player, target, heading);
                    player.setSpace(target);
                } catch (ImpossibleMoveException e) {
                }
            }
        }

    }

    /**
     * Iterates through all players on the board, performing any field actions present on the spaces they occupy.
     * Each action is executed using a new instance of GameController,
     * applying the action's effects based on the current game state.
     * @author Mohammad Haashir Khan
     */


    public void performFieldActions() {
        int i;
        for (i = 0; i < board.getPlayersNumber(); ++i) {
            Player player = board.getPlayer(i);
            Space space = player.getSpace();
            for (FieldAction action : space.getActions()) {
                GameController gameController = new GameController(board);
                action.doAction(gameController, space);
            }
        }
    }


    protected void endedGame(Player player){
        ButtonType ok = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType exitGame = new ButtonType("Exit Roborally", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "The winner is: "+ player.getName() ,ok, exitGame);
        alert.setTitle("Game Ended!");
        Optional<ButtonType> re = alert.showAndWait();


        if(re.get() != ok){
            Platform.exit();
        }
        else{
            setGameOver();
            return;
        }
    }

    private void setGameOver() {
    }


    /**
     * This method moves the player 3 spaces foward, by calling movefoward 3 times
     *
     * @param player is the the player that fast forward
     * @author Amaan Ahmed
     */
    public void fastForward(@NotNull Player player) {
        this.moveForward(player);
        this.moveForward(player);
        this.moveForward(player);
    }

    /**
     * Turns the robot direction right
     *
     * @param player is the current players robot
     * @author Amaan Ahmed
     */
    public void turnRight(@NotNull Player player) {
        Heading heading = player.getHeading();
        player.setHeading(heading.next());
    }
    /**
     * Turns the robot direction left
     *
     * @param player is the current players robot
     * @author ALi Hassan
     */
    public void turnLeft(@NotNull Player player) {
        Heading heading = player.getHeading();
        player.setHeading(heading.prev());
    }

    /**
     * Robot makes uturn
     * @Author Asim Raja
     */
    public void uTurn(@NotNull Player player) {
        int i;
        for (i = 0; i < 2; ++i) {
            Heading heading = player.getHeading();
            player.setHeading(heading.prev());
        }
    }

    /**
     * This method Moves the player 2 spaces foward
     *
     * @param player is the player that moves
     * @author Muhammed Feyez
     */
    public void move2(@NotNull Player player) {
        this.moveForward(player);
        this.moveForward(player);
    }

    /**
     * this method  moves the card from one field to another
     * @param source is the commandCard that you want to move
     * @param target is where you want to set the card
     * @return return true if it workedd
     */
    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param player
     * @param space
     * @param heading
     @throws ImpossibleMoveException
     * Attempts to move a player to a specified space in a given direction. If the space is occupied by another player,
     * it recursively tries to move the obstructing player to the next space in the same direction. If no space is available,
     * it throws an ImpossibleMoveException. This method ensures that the movement adheres to the game rules and space availability.
     * @autho Amaan Ahmed, Mohammad Haashir Khan
     */

    void moveToSpace(@NotNull Player player, @NotNull Space space, @NotNull Heading heading) throws ImpossibleMoveException {
        assert board.getNeighbour(player.getSpace(), heading) == space; // make sure the move to here is possible in principle
        Player other = space.getPlayer();
        if (other != null){
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                moveToSpace(other, target, heading);
                assert target.getPlayer() == null : target; // make sure target is free now
            } else {
                throw new ImpossibleMoveException(player, space, heading);
            } // Exepteption gør at det ikke er muligt at rykke en robot, derfor sker der ikke noget.
        }
        player.setSpace(space);
    }}