package dk.dtu.compute.se.pisd.roborally.view;


import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.*;
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class represents the view of a single space on the board in the game.
 * @author Mohammad Haashir Khan, Amaan Ahmed, Ali Hassan
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 40; // 60; // 75;
    final public static int SPACE_WIDTH = 40;  // 60; // 75;

    public final Space space;


    /**
     * Space desing and board.
     *
     * @param space
     *
     */
    public SpaceView(@NotNull Space space) {
        this.space = space;

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);

        if ((space.x + space.y) % 2 == 0) {
            this.setStyle("-fx-background-color: white;");
        } else {
            this.setStyle("-fx-background-color: black;");
        }

        updatePlayer();

        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }

    /**
     * Updates the graphical representation of a player on the board. This method first clears any existing graphics
     * and then, if a player is present in the associated space, creates a colored arrow representing the player's direction.
     * The color of the arrow is set to match the player's designated color; if an error occurs in setting the color, it defaults to medium purple.
     * The orientation of the arrow corresponds to the player's heading, rotated accordingly.
     */


    private void updatePlayer() {
        this.getChildren().clear();

        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0);
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90 * player.getHeading().ordinal()) % 360);
            this.getChildren().add(arrow);
        }
    }


    /**
     * @param subject the subject of the observer design pattern
     *
     * Opdaterer visningen baseret på ændringer i det tilknyttede space (rum). Denne metode sikrer,
     *                at kun relevante ændringer for rummet håndteres.
     * Den opdaterer spillerrepræsentationen og behandler hver action og væg i rummet for at reflektere rummets aktuelle tilstand.
     * For hver FieldAction identificeres og processeres specifikke elementer som transportbånd,
     * checkpoints og pits, hver med deres visuelle repræsentation.
     * @author Mohammad Haashir Khan
     */

    @Override
    public void updateView(Subject subject) {
        if (subject != this.space) {
            return; // Returnerer tidligt, hvis subject ikke er space
        }

        updatePlayer();

        for (FieldAction action : space.getActions()) {
            processAction(action);
        }

        for (Heading heading : space.getWalls()) {
            addwall(heading);
        }
    }

    private void processAction(FieldAction action) {
        if (action instanceof ConveyorBelt) {
            ConveyorBelt conveyorBelt = (ConveyorBelt) action;
            addConveyerbelt(conveyorBelt.getHeading());
            return;
        }

        if (action instanceof Checkpoint) {
            Checkpoint checkpoint = (Checkpoint) action;
            addCheckpoints(checkpoint.getCheckpointnumber());
            return;
        }

        if (action instanceof Pit) {
            addPit();
        }
    }

    /**
     *
     * @author Mohammad Haashir Khan
     */

    private void addCheckpoints(int checkpointNumber) {
        // Create text and circle only once, using the checkpoint number
        Text text = createText(String.valueOf(checkpointNumber));
        Circle circle = new Circle(10, 10, 10);
        circle.setFill(Color.GREENYELLOW);

        // Add created nodes to children
        this.getChildren().addAll(circle, text);
    }



    /**
     *
     * @param heading the heading of the walls
     */

    public void addwall(Heading heading) {
        /**
         *
         */

        Canvas canvas = new Canvas(SPACE_HEIGHT, SPACE_WIDTH);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        switch (heading) {


            case NORTH:
                gc.setStroke(Color.RED);
                gc.setLineWidth(5);
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.strokeLine(2, SPACE_HEIGHT - 38, SPACE_WIDTH - 2, SPACE_HEIGHT - 38);

                break;
            //nord
            case EAST:


                gc.setStroke(Color.RED);
                gc.setLineWidth(5);
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.strokeLine(38, SPACE_HEIGHT - 2, SPACE_WIDTH - 2, SPACE_HEIGHT - 38);

                break;

            //west
            case WEST:
                gc.setStroke(Color.RED);
                gc.setLineWidth(5);
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.strokeLine(2, SPACE_HEIGHT - 2, SPACE_WIDTH - 38, SPACE_HEIGHT - 38);

                break;
            //south

            case SOUTH:

                gc.setStroke(Color.RED);
                gc.setLineWidth(5);
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.strokeLine(2, SPACE_HEIGHT - 2, SPACE_WIDTH - 2, SPACE_HEIGHT - 2);

                break;

            default:
                break;
        }
        this.getChildren().add(canvas);

    }

    /**
     *
     * @param heading the heading of the Conveyerbelt
     * @author Amaan Ahmed
     */
    public void addConveyerbelt(Heading heading) {

        switch (heading) {
            case NORTH: {
                Rectangle rectangleN = new Rectangle(35, 35);
                rectangleN.setStroke(Color.RED);
                //this.getChildren().add(rectangleN);
                Polygon arrowN = new Polygon(0.0, 0.0,
                        7.5, 15.0,
                        15.0, 0.0);
                try {
                    arrowN.setFill(Color.valueOf("ORANGE"));
                } catch (Exception e) {
                    arrowN.setFill(Color.ORANGE);
                }

                arrowN.setRotate(180);
                this.getChildren().add(arrowN);
                break;
            }

            case EAST: {
                Rectangle rectangleE = new Rectangle(35, 35);
                rectangleE.setStroke(Color.RED);
                //this.getChildren().add(rectangleE);
                Polygon arrowE = new Polygon(0.0, 0.0,
                        7.5, 15.0,
                        15.0, 0.0);
                try {
                    arrowE.setFill(Color.valueOf("ORANGE"));
                } catch (Exception e) {
                    arrowE.setFill(Color.ORANGE);
                }

                arrowE.setRotate(270);
                this.getChildren().add(arrowE);
                break;
            }

            case SOUTH: {
                Rectangle rectangleS = new Rectangle(35, 35);
                rectangleS.setStroke(Color.RED);
                //this.getChildren().add(rectangleS);
                Polygon arrowS = new Polygon(0.0, 0.0,
                        7.5, 15.0,
                        15.0, 0.0);
                try {
                    arrowS.setFill(Color.valueOf("ORANGE"));
                } catch (Exception e) {
                    arrowS.setFill(Color.ORANGE);
                }

                arrowS.setRotate(0);
                this.getChildren().add(arrowS);
                break;
            }

            case WEST: {
                Rectangle rectangleW = new Rectangle(35, 35);
                rectangleW.setStroke(Color.RED);
                //this.getChildren().add(rectangleW);
                Polygon arrowW = new Polygon(0.0, 0.0,
                        7.5, 15.0,
                        15.0, 0.0);
                try {
                    arrowW.setFill(Color.valueOf("ORANGE"));
                } catch (Exception e) {
                    arrowW.setFill(Color.ORANGE);
                }

                arrowW.setRotate(90);
                this.getChildren().add(arrowW);
                break;
            }
        }
    }

    /**
     * @author Ali Hassan
     */
    public void addPit() {
        /**
         *
         */
        Rectangle circle = new Rectangle(25, 25);
        circle.setFill(Color.INDIANRED);
        this.getChildren().addAll(circle);
    }


    /**
     * @param string
     * This method is used for creating text
     * @author Mohammad Haashir Khan
     *
     */
    private Text createText(String string) {
        Text text = new Text(string);
        text.setBoundsType(TextBoundsType.VISUAL);
        text.setStyle(
                "-fx-font-family: \"Arial\";" +
                        "-fx-font-style: italic;" +
                        "-fx-font-size: 12px;"
        );
        return text;
    }
}
