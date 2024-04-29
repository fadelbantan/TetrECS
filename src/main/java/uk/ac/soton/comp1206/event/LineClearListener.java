package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.Set;

/**
 * Listener interface for receiving game loop events
 */
public interface LineClearListener {

    /**
     * Implementing classes must define the behavior for handling line clear events,
     * specifying the set of game block coordinates that are cleared.
     *
     * @param gameBlockCoordinateSet The set of coordinates of game blocks that are cleared in the line
     */
    void lineClear(Set<GameBlockCoordinate> gameBlockCoordinateSet);
}
