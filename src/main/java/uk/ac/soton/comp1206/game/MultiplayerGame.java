package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.LinkedList;
import java.util.concurrent.Executors;

public class MultiplayerGame extends Game {

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    protected Communicator communicator;
    protected GameWindow gameWindow;
    protected LinkedList<GamePiece> queue = new LinkedList<>();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, GameWindow gameWindow) {
        super(cols, rows);
        this.gameWindow = gameWindow;
    }

    public void newPiece(GamePiece gamePiece) {
        if(currentPiece == null) {
            // TODO First Piece
            currentPiece = gamePiece;
        } else if(followingPiece == null){
            // TODO Second piece
            followingPiece = gamePiece;
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        } else {
            // TODO Adds a queue
            queue.add(gamePiece);
        }
    }

    /**
     * Reassigns current and following pieces, and removing pieces from the queue
     */
    @Override
    public void nextPiece() {
        currentPiece = followingPiece;
        followingPiece = queue.remove();
        nextPieceListener.nextPiece(currentPiece, followingPiece);
        communicator.send("PIECE");
    }

    /**
     * Handles what should happen once a piece has been played, and sends a current description of the game board to the
     * server
     */
    @Override
    public void afterPiece() {
        super.afterPiece();
        StringBuilder board = new StringBuilder("BOARD ");
        for (int x = 0; x < this.getCols(); x++) {
            for(int y = 0; y < this.getRows(); y++) {
                board.append(grid.get(x, y)).append(" ");
            }
        }
        communicator.send(board.toString());
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start, and asks the server for initial pieces
     */
    @Override
    public void initialiseGame() {
        timer = Executors.newSingleThreadScheduledExecutor();
        communicator = gameWindow.getCommunicator();
        //Listens for messages from communicator and handles the command
        communicator.addListener(message -> Platform.runLater(() -> listen(message.trim())));
        for(int x = 0; x < 5; x++) {
            communicator.send("PIECE");
        }
    }

    /**
     * Increases the Score depending on the number of lines and blocks cleared. Also increments Level every 1000 points.
     * Sends score to the server.
     * @param lines
     * @param blocks
     */
    @Override
    public void score(int lines, int blocks) {
        super.score(lines, blocks);
        communicator.send("SCORE " + this.scoreProperty().get());
    }

    /**
     * Handles messages from communicator
     * @param message
     */
    protected void listen(String message) {
        if (message.contains("PIECE")) {
            logger.info("Adding piece to queue");
            message = message.replace("PIECE ", "");
            GamePiece gamePiece = GamePiece.createPiece(Integer.parseInt(message));
            newPiece(gamePiece);
        }
    }
}
