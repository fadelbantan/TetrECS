package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;

    private Multimedia multimedia = new Multimedia();

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

//        var stackPane = new StackPane();
//        stackPane.getChildren().add(pieceBoard);
//        stackPane.setAlignment(Pos.CENTER_RIGHT);
//        challengePane.getChildren().add(stackPane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);




//        // Top
//        var topBar = new HBox(200);
//        topBar.setAlignment(Pos.CENTER);
//        BorderPane.setMargin(topBar, new Insets(10, 0, 0, 0));
//        mainPane.setTop(topBar);
//
//        // Score
//        var scoreBox = new VBox();
//        scoreBox.setAlignment(Pos.CENTER);
//        var scoreText = new Text("Score");
//        scoreText.getStyleClass().add("heading");
//        var scoreNum = new Text();
//        scoreNum.getStyleClass().add("score");
//        scoreNum.textProperty().bind(game.score.asString());
//        scoreBox.getChildren().addAll(scoreText, scoreNum);
//
//        // Title
//        var title = new Text("Single Player");
//        title.getStyleClass().add("title");
//
//        // Lives
//        var livesBox = new VBox();
//        livesBox.setAlignment(Pos.CENTER);
//        var livesText = new Text("Lives");
//        livesText.getStyleClass().add("heading");
//        var livesNum = new Text();
//        livesNum.getStyleClass().add("lives");
//        livesNum.textProperty().bind(game.lives.asString());
//        livesBox.getChildren().addAll(livesText, livesNum);
//
//        topBar.getChildren().addAll(scoreBox, title, livesBox);
//
//        /*
//         Left
//         */
//        VBox leftBar = new VBox();
//        leftBar.setAlignment(Pos.CENTER);
//        leftBar.setPadding(new Insets(0, 0, 0, 20));
//        mainPane.setLeft(leftBar);
//
//        // Test
//        var test = new Text("test");
//        test.getStyleClass().add("heading");
//
//        leftBar.getChildren().addAll(test);
//
//        /*
//         Right
//         */
//        var rightBar = new VBox();
//        rightBar.setAlignment(Pos.CENTER);
//        rightBar.setPadding(new Insets(0, 20, 0, 0));
//        mainPane.setRight(rightBar);
//
//        // High score
//        var highScoreText = new Text("High Score");
//        highScoreText.getStyleClass().add("heading");
//        var highScoreNum = new Text();
//        highScoreNum.getStyleClass().add("hiscore");
//
//        // Level
//        var levelText = new Text("Level");
//        levelText.getStyleClass().add("heading");
//        var levelNum = new Text();
//        levelNum.getStyleClass().add("level");
//        levelNum.textProperty().bind(game.level.asString());
//
//        // Multiplier
//        var multiplierText = new Text("Multiplier");
//        multiplierText.getStyleClass().add("heading");
//        var multiplierNum = new Text();
//        multiplierNum.getStyleClass().add("heading");
//        multiplierNum.textProperty().bind(game.multiplier.asString());
//
//        // Incoming piece
//        var incomingText = new Text("Incoming");
//        incomingText.getStyleClass().add("heading");
//        //currentPiece = new PieceBoard(GamePiece.createPiece(), 100,100);
//        currentPiece.getStyleClass().add("gameBox");
//
//        rightBar.getChildren().addAll(highScoreText, highScoreNum, levelText, levelNum, multiplierText, multiplierNum, incomingText, currentPiece);
//        var board = new GameBoard(game.getGrid(), (float) gameWindow.getWidth() / 2, (float) gameWindow.getWidth() / 2);
//        mainPane.setCenter(board);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        //Handle block on GameBoard grid being clicked
        board.setOnBlockClick(this::blockClicked);
    }

    //Setting Piece Listener
//        game.setNextPieceListener(this::nextPiece);
//
// Setting Right Clicked Listener
//        board.setOnRightClicked(this::rotate);
//
//        pieceBoard.setOnBlockClick(this::rotate);
//
//        followingPieceBoard.setOnBlockClick(this::swapPieces);
//
//        scene.setOnKeyPressed(this::keyboardInput);

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
