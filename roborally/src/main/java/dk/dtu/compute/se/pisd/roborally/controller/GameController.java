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
         *starts the programmingphase
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
     * finishes the programmingphase
     */
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    /**
     *makes the programfield visible
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
     * makes the programfield unvisible
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
     * execute the programs
     */
    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    /**
     * execute a step
     */
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    /**
     * continues the program
     */
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }
    /**
     * executes a commandcard and goes to the next player. if the board is not in activation phase, then it won't work.
     * and there is a currentplayer from the board. the step has to be between those 5 cards. if the card is null,
     * the it won't go through. after executing command, then it will go to the next player by setting currentplayer on board,
     * to the next player
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
     * commandcards where you have a choice between to options.
     * @param option is the cards that is an option
     *
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
     * Moves the current players robot one space i the robots current direction
     *
     * @param player The player which Robot is getting moved one space in the current direction
     *               This method first insures that both the current space and the neigbouring space exist on the board
     *               It then checks if the neighbouring space is occupied by another player both either setting the players space to
     *               the neighbouring space or if the space is occupied it will run a method called moveToSpace which pushes the other
     *               player away before moving onto the space
     *
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
                    player.setHeading(Heading.EAST);
                } catch (ImpossibleMoveException e) {
                }
            }
        }

    }


    /**
     * It is here where our action fields are activated after the players' turn is finished.
     * By examining where the players are located and which Fieldaction they are on, we then call doAction.

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
     *
     * The method moves the current robot 3 spaces forward in the robots current direction.
     * Before moving the robot the method checks if every space is free.
     *
     * @param player is the the player that fast forward
     */
    public void fastForward(@NotNull Player player) {
        this.moveForward(player);
        this.moveForward(player);
        this.moveForward(player);
    }

    /**
     * The robots direction turns to the right
     *
     * @param player is the current players robot
     */
    public void turnRight(@NotNull Player player) {
        Heading heading = player.getHeading();
        player.setHeading(heading.next());
    }

    /**
     * The robots direction turns to the left
     * @param player is the current players robot
     *
     */
    public void turnLeft(@NotNull Player player) {
        Heading heading = player.getHeading();
        player.setHeading(heading.prev());
    }

    /**
     * @param player is the current players robot
     * The robots direction turns around
     */
    public void uTurn(@NotNull Player player) {
        int i;
        for (i = 0; i < 2; ++i) {
            Heading heading = player.getHeading();
            player.setHeading(heading.prev());
        }
    }

    /**
     * this moves the player 2 spaces forward
     * @param player is the player that moves
     */
    public void move2(@NotNull Player player) {
        this.moveForward(player);
        this.moveForward(player);
    }

    /**
     * this moves the card from one place to another.
     * @param source is the commanrdfard that you want to move
     * @param target is where you want to set the card
     * @return return true if it workedbbb
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
     * @throws ImpossibleMoveException
     *
     *
     *
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
            }
        }
        player.setSpace(space);
    }



}
