package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

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

        pieceBoard = new GameBoard(3,3,100,100);
        pieceBoard.setAlignment(Pos.CENTER);

        followingPieceBoard = new GameBoard(3,3,75,75);
        followingPieceBoard.setAlignment(Pos.CENTER);
        pieceBoard.setTranslateY(-10);
        pieceBoard.setTranslateX(12.5);

        var pieces = new VBox(pieceBoard, followingPieceBoard);
        pieces.setAlignment(Pos.CENTER_RIGHT);
        pieces.setTranslateX(-75);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        mainPane.setRight(pieces);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        //Handle block on GameBoard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        // Setting Piece Listener
        game.setNextPieceListener(this::nextPiece);

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
        Boolean piecePlayed = game.blockClicked(gameBlock);
        if (piecePlayed) {
            Multimedia.playAudio("place.wav");
        } else {
            Multimedia.playAudio("fail.wav");
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
        Multimedia.playMusic("game.wav");


        scene.setOnKeyPressed(this::keyboardInput);
        blockX = 0;
        blockY = 0;
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
        Multimedia.playAudio("rotate.wav");
    }

    protected void swapPieces(GameBlock gameBlock) {
        swapPieces();
    }
    protected void swapPieces() {
        game.swapCurrentPiece();
        pieceBoard.pieceToDisplay(game.getCurrentPiece());
        followingPieceBoard.pieceToDisplay(game.getFollowingPiece());
        Multimedia.playAudio("rotate.wav");
    }
    protected void keyboardInput(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ESCAPE) {
            Multimedia.stopBackground();
            gameWindow.startMenu();
            logger.info("Escape Pressed");
        } else if(keyEvent.getCode() == KeyCode.Q || keyEvent.getCode() == KeyCode.Z || keyEvent.getCode() == KeyCode.OPEN_BRACKET) {
            rotateLeft();
        } else if (keyEvent.getCode() == KeyCode.E || keyEvent.getCode() == KeyCode.C || keyEvent.getCode() == KeyCode.CLOSE_BRACKET ) {
            rotate(1);
        } else if(keyEvent.getCode() == KeyCode.SPACE || keyEvent.getCode() == KeyCode.R) {
            swapPieces();
        } else if(keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.X) {
            blockClicked(board.getBlock(blockX, blockY));
        } else if(keyEvent.getCode() == KeyCode.W || keyEvent.getCode() == KeyCode.UP) {
            if(blockY>0) {
                blockY-=1;
            } else {
                Multimedia.playAudio("fail.wav");
            }
        } else if(keyEvent.getCode() == KeyCode.D || keyEvent.getCode() == KeyCode.RIGHT) {
            if(blockX<5) {
                blockX+=1;
            } else {
                Multimedia.playAudio("fail.wav");
            }
        } else if(keyEvent.getCode() == KeyCode.S || keyEvent.getCode() == KeyCode.DOWN) {
            if(blockY<5) {
                blockY+=1;
            } else {
                Multimedia.playAudio("fail.wav");
            }
        } else if(keyEvent.getCode() == KeyCode.A|| keyEvent.getCode() == KeyCode.LEFT) {
            if(blockX>0) {
                blockX-=1;
            } else {
                Multimedia.playAudio("fail.wav");
            }
        }

    }
}
