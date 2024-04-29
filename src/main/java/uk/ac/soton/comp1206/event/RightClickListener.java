package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;

/**
 * Listener interface for receiving right-click events on game blocks
 */
public interface RightClickListener {

    /**
     * Implementing classes must define the behavior for handling right-click events,
     * specifying the game block that was right-clicked
     *
     * @param block The game block that was right-clicked
     */
    void rightClick(GameBlock block);
}
