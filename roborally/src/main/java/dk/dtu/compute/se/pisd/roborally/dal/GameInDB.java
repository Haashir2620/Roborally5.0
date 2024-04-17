package dk.dtu.compute.se.pisd.roborally.dal;

/**
 * @author Ekkart Kindler, ekki@dtu.dk
 * a class that is called GameInDB that includes an id and a name.
 */
public class GameInDB {
    public final int id;
    public final String name;

    public GameInDB(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return id + ": " + name;
    }

}
