package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameEndListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * The current GamePiece being played
     */
    protected GamePiece currentPiece;

    /**
     * The next GamePiece to be played
     */
    protected GamePiece followingPiece;

    /**
     * Current Score of the player
     */
    protected IntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * Current Level of the game
     */
    protected IntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * Current Lives left of the player
     */
    protected IntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * Current Game Multiplier
     */
    protected IntegerProperty multiplier = new SimpleIntegerProperty(1);

    //Listeners used for Game Logic
    protected NextPieceListener nextPieceListener;
    protected LineClearListener lineClearListener;
    protected GameLoopListener gameLoopListener;
    protected GameEndListener gameEndListener;

    /**
     * Timer - detects when a turn should end
     */
    protected ScheduledExecutorService timer;
    protected int initialDelay = 12000;
    /**
     * Used to initiate new timer when a loop has finished
     */
    protected ScheduledFuture<?> newLoop;

    /**
     * ArrayList of Local Scores available
     */
    protected ArrayList<Pair<String, Integer>> scores = new ArrayList<>();

    /**
     * Multimedia class
     */
    protected Multimedia multimedia = new Multimedia();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    public IntegerProperty livesProperty() {
        return lives;
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public IntegerProperty multiplierProperty() {
        return multiplier;
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        startLoop();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        followingPiece = spawnPiece();
        nextPiece();
        timer = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     * @return True or False whether a block has been clicked and placed
     */
    public boolean blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        if(grid.canPlayPiece(currentPiece, x, y)) {
            grid.playPiece(currentPiece, x, y);
            nextPiece();
            afterPiece();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Handles what should happen after a piece is played
     */
    public void afterPiece() {
        int lines = 0;
        HashSet<GameBlockCoordinate> blocksToBeCleared = new HashSet<>();

        // Vertical Lines
        for(int x=0; x < cols; x++) {
            int countX = 0;
            for(int y=0; y < rows; y++) {
                if(grid.get(x,y) == 0) break;
                countX+=1;
            }
            if(countX == rows) {
                lines+=1;
                for(int y=0; y < rows; y++) {
                    GameBlockCoordinate coordinate = new GameBlockCoordinate(x,y);
                    // Add all GameBlockCoordinates to HashSet to be cleared
                    blocksToBeCleared.add(coordinate);
                }
            }
        }

        // Horizontal Lines
        for(int y=0; y < rows; y++) {
            int countY = 0;
            for(int x=0; x < cols; x++) {
                if(grid.get(x,y) == 0) break;
                countY+=1;
            }
            if(countY == cols) {
                lines+=1;
                for(int x=0; x < cols; x++) {
                    GameBlockCoordinate coordinate = new GameBlockCoordinate(x,y);
                    // Add all GameBlockCoordinates to HashSet to be cleared
                    blocksToBeCleared.add(coordinate);
                }
            }
        }

        // If there is a line to clear
        if(lines>0){
            // Clears Blocks
            clear(blocksToBeCleared);
            // Increments Score
            score(lines, blocksToBeCleared.size());
            this.multiplier.set(this.multiplier.add(1).get());
            // Increments Multiplier
            if(lineClearListener != null) {
                lineClearListener.lineClear(blocksToBeCleared);
                //Calls Listener
                logger.info("Clear Lines");
            }
        } else {
            //Resets Multiplier
            this.multiplier.set(1);
        }
    }

    /**
     * Iterates through a given HashSet of GameBlockCoordinates and clears the GameBlock
     *
     * @param blocks A HashSet Of Blocks to be set to 0
     */
    public void clear(HashSet<GameBlockCoordinate> blocks) {
        for (GameBlockCoordinate block: blocks) {
            grid.set(block.getX(), block.getY(), 0);
        }
    }

    /**
     * Increases the Score depending on the number of lines and blocks cleared.
     * Also increments Level every 1000 points
     *
     * @param lines Number of Lines Cleared
     * @param blocks Number of Blocks Cleared
     */
    public void score(int lines, int blocks){
        int scoreToAdd = lines*blocks*10*this.multiplier.get();
        this.score.set(this.score.add(scoreToAdd).get());
        logger.info("Score added, Score: " + this.scoreProperty().get());
        int level = this.score.get() / 1000;
        if(this.level.get() != level) {
            this.level.set(level);
            multimedia.playSound("level.wav");
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     *
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     *
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     *
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Creates a new Random Piece
     *
     * @return A new GamePiece
     */
    public GamePiece spawnPiece() {
        Random random = new Random();
        int randomNum = random.nextInt(15);
        GamePiece gamePiece = GamePiece.createPiece(randomNum);
        return gamePiece;
    }

    /**
     * Reassigns currentPiece and followingPiece after a piece has been played
     */
    public void nextPiece() {
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece, followingPiece);
    }

    /**
     * Sets the listener for receiving notifications about the next game piece
     *
     * @param nextPieceListener The listener for next piece events
     */
    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
    }

    /**
     * Sets the listener for receiving line clear events
     *
     * @param lineClearListener The listener for line clear events
     */
    public void setLineClearListener(LineClearListener lineClearListener) {
        this.lineClearListener = lineClearListener;
    }

    /**
     * Sets the listener for receiving game loop events
     *
     * @param gameLoopListener The listener for game loop events
     */
    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    /**
     *Sets the listener for receiving game end events
     *
     * @param gameEndListener The listener for game end events
     */
    public void setGameEndListener(GameEndListener gameEndListener) {
        this.gameEndListener = gameEndListener;
    }

    /**
     * Rotates the currentPiece
     */
    public void rotateCurrentPiece() {
        currentPiece.rotate();
    };

    /**
     * Swaps currentPiece and followingPiece
     */
    public void swapCurrentPiece() {
        GamePiece temp = followingPiece;
        followingPiece = currentPiece;
        currentPiece = temp;
    }

    /**
     * Returns the currentPiece
     *
     * @return currentPiece
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    /**
     * Returns the followingPiece
     *
     * @return followingPiece
     */
    public GamePiece getFollowingPiece() {
        return followingPiece;
    }

    /**
     * Returns the timerDelay, which is calculated based on level
     *
     * @return How long th timer should last in ms
     */
    public int getTimerDelay() {
        int delay = initialDelay - (500 * level.get());
        return Math.max(delay, 2500);
    }

    /**
     * Triggers GameLoop when the player does not play a piece
     */
    public void gameLoop() {
        nextPiece();
        if(lives.get() == 0) {
            gameOver();
        } else {
            lives.set(lives.get() - 1);
            multimedia.playSound("lifelose.wav");
            multiplier.set(1);
        }
        if(gameLoopListener != null){
            gameLoopListener.gameLoop(getTimerDelay());
        }
        startLoop();
    }

    /**
     * Starts a new timer
     */
    public void startLoop() {
        newLoop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        gameLoopListener.gameLoop(getTimerDelay());
    }

    /**
     * Restarts timer when the player has played a piece
     */
    public void restartLoop() {
        newLoop.cancel(false);
        startLoop();
    }

    /**
     * Ends the Game
     */
    public void endGame() {
        logger.info("Game Has Ended");
        timer.shutdownNow();
    }

    /**
     * Calls the gameEndListener when a game has ended
     */
    public void gameOver() {
        if(gameEndListener != null){
            Platform.runLater(() -> gameEndListener.gameEnd(this));
        }
    }
}
