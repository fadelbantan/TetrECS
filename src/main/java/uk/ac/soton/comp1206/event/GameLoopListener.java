package uk.ac.soton.comp1206.event;
/**
 * Listener interface for receiving game loop events
 */
public interface GameLoopListener {

    /**
     * Implementing classes must define the behavior for the game loop event,
     * specifying the delay between iterations.
     *
     * @param delay The delay between iterations of the game loop, in milliseconds
     */
    void gameLoop(int delay);
}
