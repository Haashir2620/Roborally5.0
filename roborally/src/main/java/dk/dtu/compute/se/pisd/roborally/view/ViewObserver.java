package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import javafx.application.Platform;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public interface ViewObserver extends Observer {

    void updateView(Subject subject);

    @Override
    default void update(Subject subject) {
        // This default implementation of the update method makes sure that ViewObserver implementations
        // are doing the update only in the FX application thread. The update of the view is instead
        // done in the updateView() method;
        if (Platform.isFxApplicationThread()) {
            updateView(subject);
        } else {
            Platform.runLater(() -> updateView(subject));
        }
    }

}
