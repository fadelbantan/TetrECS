package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.ui.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Set;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;

    protected Multimedia multimedia = new Multimedia();

    protected GameBoard pieceBoard;

    protected GameBoard followingPieceBoard;

    protected GameBoard board;

    protected int blockX;

    protected int blockY;

    protected Rectangle timer;


    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        this.scene = gameWindow.getScene();

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var score = new Text("Score: " + game.scoreProperty().asString().getValue());
        var level = new Text("Level: " + game.levelProperty().asString().getValue());
        var multiplier = new Text("Multiplier: " + game.multiplierProperty().asString().getValue());
        var lives = new Text("Lives: " + game.livesProperty().asString().getValue());

        score.getStyleClass().add("heading");
        level.getStyleClass().add("heading");
        multiplier.getStyleClass().add("heading");
        lives.getStyleClass().add("heading");

        HBox statistics = new HBox(20, score, level, multiplier, lives);

        challengePane.getChildren().add(statistics);
        statistics.setAlignment(Pos.TOP_CENTER);
        statistics.setTranslateY(20);

        pieceBoard = new GameBoard(3,3,100,100);
        pieceBoard.setAlignment(Pos.CENTER);

        followingPieceBoard = new GameBoard(3,3,75,75);

        followingPieceBoard.setAlignment(Pos.CENTER);
        pieceBoard.setTranslateY(-10);
        pieceBoard.setTranslateX(12.5);

        var pieces = new VBox(pieceBoard, followingPieceBoard);
        pieces.setAlignment(Pos.CENTER_RIGHT);
        pieces.setTranslateX(-75);

        timer = new Rectangle(gameWindow.getWidth(),10);

        var timePane = new StackPane();
        timePane.getChildren().add(timer);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        mainPane.setRight(pieces);
        mainPane.setTop(timePane);
        timePane.setAlignment(Pos.TOP_LEFT);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        //Handle block on GameBoard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        // Setting Piece Listener
        game.setNextPieceListener(this::nextPiece);

        game.setLineClearedListener(this::lineClear);

        game.setOnGameLoop(this::gameLoop);

        // Setting Right Click Listener
        board.setOnRightClicked(this::rotate);

        pieceBoard.setOnBlockClick(this::rotate);

        followingPieceBoard.setOnBlockClick(this::swapPieces);

        scene.setOnKeyPressed(this::keyboardInput);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        boolean piecePlayed = game.blockClicked(gameBlock);
        if (piecePlayed) {
            multimedia.playAudio("place.wav");
            board.getBlock(blockX, blockY).paintCursor();
            game.restartLoop();
        } else {
            multimedia.playAudio("fail.wav");
        }    }

    /**
     * Set up the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();
        multimedia.playMusic("game.wav");


        scene.setOnKeyPressed(this::keyboardInput);
        blockX = 0;
        blockY = 0;
        board.getBlock(blockX, blockY).paintCursor();
    }
    protected void nextPiece(GamePiece gamePiece, GamePiece followingGamePiece) {
        pieceBoard.pieceToDisplay(gamePiece);
        followingPieceBoard.pieceToDisplay(followingGamePiece);
    }

    protected void rotate(GameBlock gameBlock) {
        rotate(1);
    }

    protected void rotateLeft() {
        rotate(3);
    }

    protected void rotate(int rotations) {
        for(int x = 0; x< rotations; x++) {
            game.rotateCurrentPiece();
        }
        pieceBoard.pieceToDisplay(game.getCurrentPiece());
        multimedia.playAudio("rotate.wav");
    }

    protected void swapPieces(GameBlock gameBlock) {
        swapPieces();
    }

    protected void swapPieces() {
        game.swapCurrentPiece();
        pieceBoard.pieceToDisplay(game.getCurrentPiece());
        followingPieceBoard.pieceToDisplay(game.getFollowingPiece());
        multimedia.playAudio("rotate.wav");
    }
    protected void keyboardInput(KeyEvent keyEvent) {
        int oldBlockX = blockX;
        int oldBlockY = blockY;
        boolean moved = false;

        if(keyEvent.getCode() == KeyCode.ESCAPE) {
            multimedia.stopBackground();
            gameWindow.startMenu();
            logger.info("Escape Pressed");
        } else if(keyEvent.getCode() == KeyCode.Q || keyEvent.getCode() == KeyCode.Z || keyEvent.getCode() == KeyCode.OPEN_BRACKET) {
            rotateLeft();
        } else if(keyEvent.getCode() == KeyCode.E || keyEvent.getCode() == KeyCode.C || keyEvent.getCode() == KeyCode.CLOSE_BRACKET ) {
            rotate(1);
        } else if(keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.R) {
            swapPieces();
        } else if(keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.X) {
            blockClicked(board.getBlock(blockX, blockY));
        } else if(keyEvent.getCode() == KeyCode.W || keyEvent.getCode() == KeyCode.UP) {
            if(blockY>0) {
                blockY-=1;
                moved = true;
            } else {
                multimedia.playAudio("fail.wav");
            }
        } else if(keyEvent.getCode() == KeyCode.D || keyEvent.getCode() == KeyCode.RIGHT) {
            if(blockX<4) {
                blockX+=1;
                moved = true;
            } else {
                multimedia.playAudio("fail.wav");
            }
        } else if(keyEvent.getCode() == KeyCode.S || keyEvent.getCode() == KeyCode.DOWN) {
            if(blockY<4) {
                blockY+=1;
                moved = true;
            } else {
                multimedia.playAudio("fail.wav");
            }
        } else if(keyEvent.getCode() == KeyCode.A|| keyEvent.getCode() == KeyCode.LEFT) {
            if(blockX>0) {
                blockX-=1;
                moved = true;
            } else {
                multimedia.playAudio("fail.wav");
            }
        } if(moved) {
            board.getBlock(oldBlockX, oldBlockY).resetCursor();
            board.getBlock(blockX, blockY).paintCursor();
        }
    }
    protected void lineClear(Set<GameBlockCoordinate> gameBlockCoordinates) {
        multimedia.playAudio("clear.wav");
        board.fadeOut(gameBlockCoordinates);
    }

    protected void gameLoop(int delay) {
        timer.widthProperty().set(gameWindow.getWidth());
        Timeline timerBar = createTimeLine(delay);
        timerBar.play();
    }

    private Timeline createTimeLine(int delay) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(timer.fillProperty(), Color.GREEN)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(timer.widthProperty(), timer.getWidth())));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay*0.5), new KeyValue(timer.fillProperty(), Color.YELLOW)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay*0.5), new KeyValue(timer.widthProperty(), timer.getWidth()*0.75)));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay*0.75), new KeyValue(timer.fillProperty(), Color.RED)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay*0.75), new KeyValue(timer.widthProperty(), timer.getWidth()*0.5)));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay), new KeyValue(timer.widthProperty(), 0)));

        return timeline;
    }
}
