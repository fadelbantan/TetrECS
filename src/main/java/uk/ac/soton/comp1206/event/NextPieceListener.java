package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Listener interface for receiving notifications about the next game piece
 */
public interface NextPieceListener {

    /**
     * Implementing classes must define the behavior for handling the next piece event,
     * specifying the next game piece and the following game piece.
     *
     * @param nextGamePiece The next game piece to be displayed
     * @param followingGamePiece The following game piece to be displayed after the next game piece
     */
    void nextPiece(GamePiece nextGamePiece, GamePiece followingGamePiece);

}
