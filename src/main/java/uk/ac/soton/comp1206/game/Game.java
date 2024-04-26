package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.*;


import static uk.ac.soton.comp1206.game.GamePiece.*;


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
     * Track what the current piece is
     */
    protected GamePiece currentPiece;
    protected GamePiece followingPiece;


    protected NextPieceListener nextPieceListener;
    protected LineClearListener lineClearedListener;
    protected GameLoopListener gameLoopListener;

    protected ScheduledExecutorService timer;
    protected int initialDelay = 12000;
    protected ScheduledFuture<?> newLoop;

    /**
     *  Initial values
     */
    public IntegerProperty score = new SimpleIntegerProperty(0);
    public IntegerProperty level = new SimpleIntegerProperty(0);
    public IntegerProperty lives = new SimpleIntegerProperty(3);
    public IntegerProperty multiplier = new SimpleIntegerProperty(1);

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
     *
     * @param gameBlock the block that was clicked
     */
    public boolean blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        // If a piece cannot be played it should be remembered until the timer runs out
        if (grid.canPlayPiece(currentPiece, x, y)) {
            grid.playPiece(currentPiece, x, y);
            nextPiece();
            afterPiece();
            return true;
            //place.wav
        } else {
            return false;
            //fail.wav
        }
    }

    /**
     * TODO HEREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
     * @param blocks
     */
    public void clear(HashSet<GameBlockCoordinate> blocks) {
        for (GameBlockCoordinate block: blocks) {
            grid.set(block.getX(), block.getY(), 0);
        }
    }

    /**
     * Manages clearing the lines
     */
    public void afterPiece() {
        int linesCleared = 0;
        HashSet<GameBlockCoordinate> clearBlocks = new HashSet<>();

        // Clearing horizontal lines
        for (int x = 0; x < cols; x++) {
            int counter = 0;
            for (int y = 0; y < rows; y++) {
                if (grid.get(x, y) == 0) break;
                counter++;
            }
            if (counter == rows) {
                linesCleared++;
                for (int y = 0; y < rows; y++) {
                    GameBlockCoordinate coordinate = new GameBlockCoordinate(x, y);
                    clearBlocks.add(coordinate);
                }
            }
        }

        // Clearing vertical lines
        for (int y = 0; y < rows; y++) {
            int  counter = 0;
            for (int x = 0; x < cols; x++) {
                if (grid.get(x, y) == 0) break;
                counter++;
            }
            if (counter == cols) {
                linesCleared++;
                for (int x = 0; x < cols; x++) {
                    GameBlockCoordinate coordinate = new GameBlockCoordinate(x, y);
                    clearBlocks.add(coordinate);
                }
            }
        }
        if(linesCleared>0) {
            clear(clearBlocks);
            score(linesCleared, clearBlocks.size());
            this.multiplier.set(this.multiplier.add(1).get());
            if(lineClearedListener != null) {
                lineClearedListener.lineClear(clearBlocks);
                logger.info("Clear Lines");
            }
        } else {
            this.multiplier.set(1);
        }
    }

    /**
     * Calculate the score
     * @param lines number of lines cleared
     * @param blocks number of grid blocks cleared
     */
    public void score(int lines, int blocks) {
        int scoreToAdd = lines * blocks * 10 * this.multiplier.get();
        this.score.set(this.score.add(scoreToAdd).get());
        logger.info("Score added, Score: " + this.scoreProperty().get());
        int level = this.score.get() / 1000;
        if (this.level.get() != level) {
            this.level.set(level);
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Randomise generating the pieces
     * @return GamePiece
     */
    public GamePiece spawnPiece() {
        Random random = new Random();
        GamePiece piece = GamePiece.createPiece(random.nextInt(PIECES));
        return piece;
    }

    /**
     * After placing, replace the piece with a new piece
     */
    public void nextPiece() {
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece, followingPiece);
    }

    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
    }

    public void setLineClearedListener(LineClearListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    public void rotateCurrentPiece() {
        logger.info("The current piece {} has been rotated ", currentPiece.toString());
        currentPiece.rotate();

    }

    public void swapCurrentPiece() {
        GamePiece tempGamePiece = followingPiece;
        followingPiece = currentPiece;
        currentPiece = tempGamePiece;
        logger.info("Pieces swapped");
    }

    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    public GamePiece getFollowingPiece() {
        return followingPiece;
    }

    public int getTimerDelay() {
        int delay = initialDelay - (500 * level.get());
        return Math.max(delay, 2500);
    }

    public void gameLoop() {
        nextPiece();
        if(lives.get() == 1) {
            lives.set(0);
            endGame();
        }
        lives.set(lives.get() - 1);
        multiplier.set(1);
        if(gameLoopListener != null) {
            gameLoopListener.gameLoop(getTimerDelay());
        }
        startLoop();
    }

    public void startLoop() {
        newLoop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);

        gameLoopListener.gameLoop(getTimerDelay());
    }

    public void restartLoop() {
        newLoop.cancel(false);
        startLoop();
    }

    public void endGame() {
        timer.shutdownNow();
        logger.info("Game has ended");
    }

    /**
     * Skip current piece with 100 points
     */
//    public void skipPiece() {
//        if (score.get() >= 50) {
//            score.set(score.get() - 50);
//            nextPiece();
//            Multimedia.playAudio("transition.wav");
//            logger.info("Skipped piece");
//        } else {
//            Multimedia.playAudio("fail.wav");
//            logger.info("Not enough points");
//        }
//    }

    /**
     * Buy one life with 300 points
     */
//    public void addLives() {
//        if (score.get() >= 100) {
//            score.set(score.get() - 100);
//            lives.set(lives.get() + 1);
//            Multimedia.playAudio("lifegain.wav");
//            logger.info("Added one life");
//        } else {
//            Multimedia.playAudio("fail.wav");
//            logger.info("Not enough points");
//        }
//    }

    /**
     * Clear the whole grid with 500 points
     */
//    public void clearAll() {
//        if (score.get() >= 200) {
//            score.set(score.get() - 200);
//            grid.clean();
//            multiplier.set(multiplier.add(1).get());
//            Multimedia.playAudio("explode.wav");
//            logger.info("Grid cleaned");
//        } else {
//            Multimedia.playAudio("fail.wav");
//            logger.info("Not enough points");
//        }
//    }

}
