package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;

/**
 * The Game End Listener is used to check when the game ends
 */
public interface GameEndListener {

    /**
     * Called when a game ends
     *
     * @param game The game that has ended
     */
    void gameEnd(Game game);
}
