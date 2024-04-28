package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Scores;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MultiplayerScene extends ChallengeScene {
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    protected VBox messagesBox;

    protected Communicator communicator;

    protected TextField textField = new TextField();

    protected SimpleListProperty<Pair<String, Integer>> multiplayerScores = new SimpleListProperty<>();

    protected Scores leaderboard;

    protected VBox boardSideBar = new VBox();

    protected Set<String> players;

    protected HashMap<String, GameBoard> playerToGameBoard;


    public MultiplayerScene(GameWindow gameWindow, Set<String> playerSet) {
        super(gameWindow);
        this.multiplayerScores.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        this.players = playerSet;
    }

    @Override
    public void initialise() {
        super.initialise();
        communicator = gameWindow.getCommunicator();
        //Listens for messages from communicator and handles the command
        communicator.addListener(message -> Platform.runLater(() -> listen(message.trim())));
        communicator.send("SCORES");
        initialisePlayerBoards();
    }


    @Override
    protected void keyboardInput(KeyEvent keyEvent) {
        super.keyboardInput(keyEvent);
        // TODO Opens Chat
        if (keyEvent.getCode() == KeyCode.T) {
            if (!textField.isVisible()) {
                textField.setVisible(true);
                String message = textField.getText();
                if (message != null) {
                    communicator.send("MSG " + message);
                    textField.clear();
                }
            } else {
                textField.setVisible(false);
                textField.clear();
            }
        }
        // TODO Exits scene
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            if (textField.isVisible()) {
                textField.setVisible(false);
                textField.clear();
            } else {
                multimedia.stopBackground();
                game.endGame();
                multimedia.playAudio("transition.wav");
                gameEnd();
                gameWindow.startMenu();
                communicator.send("DIE");
                logger.info("Escape Pressed");
            }
        }
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (textField.isVisible()) {
                String message = textField.getText();
                if (message != null) {
                    communicator.send("MSG " + message);
                    textField.clear();
                }
                textField.setVisible(false);
            }
        }
    }

    @Override
    public void setupGame() {
        logger.info("Staring a new multiplayer game");
        game = new MultiplayerGame(5, 5, this.gameWindow);
    }

    @Override
    public void build() {

        var messagesPane = new BorderPane();
        messagesPane.setPrefSize(gameWindow.getWidth()/8, gameWindow.getHeight()/8);

        var currentMessages = new ScrollPane();
        currentMessages.setBackground(null);
        currentMessages.setPrefSize(messagesPane.getWidth(), messagesPane.getHeight()-100);
        currentMessages.needsLayoutProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                currentMessages.setVvalue(1.0);
            }
        });

        messagesBox = new VBox();
        messagesBox.getStyleClass().add("messages");
        messagesBox.setPrefSize(currentMessages.getPrefWidth(), currentMessages.getPrefHeight());
        VBox.setVgrow(currentMessages, Priority.ALWAYS);

        super.build();

        currentMessages.setContent(messagesBox);
        messagesPane.setCenter(currentMessages);

        var messageEntry = new TextField();
        var messageConfirm = new Button("Send");

        var chatHeading = new Text("Chat: <Press T to Chat>");
        chatHeading.setTextAlignment(TextAlignment.CENTER);
        chatHeading.getStyleClass().add("heading");
        currentMessages.getStyleClass().add("scroller");

        var chatBox = new HBox(chatHeading, textField);
        chatBox.setMaxWidth(gameWindow.getWidth());
        textField.setPrefWidth(gameWindow.getWidth() - chatHeading.getLayoutBounds().getWidth());
        textField.setAlignment(Pos.CENTER);
        textField.setVisible(false);

        var chat = new VBox(chatBox, messagesPane);

        // TODO Shows the score of players in the game
        leaderboard = new Scores();
        leaderboard.setAlignment(Pos.CENTER);
        leaderboard.setTranslateY(-50);
        leaderboard.setTranslateX(25);
        this.multiplayerScores.bind(leaderboard.listProperty());


        var sideBar = new VBox(leaderboard, pieceBoard, followingPieceBoard);

        sideBar.setAlignment(Pos.CENTER_RIGHT);
        sideBar.setTranslateX(-75);

        mainPane.setRight(sideBar);

        mainPane.setBottom(chat);

        // TODO Setting GameEndListener
        game.setGameEndListener(game -> {
            gameEnd();
            gameWindow.startScores(game);
        });

    }

    protected void endUser(String userName) {
        leaderboard.strikeThrough(userName);
    }

    protected void listen(String s) {
        if(s.contains("MSG")) {
            s = s.replace("MSG ", "");
            String[] messageArr = s.split(":");
            if (messageArr.length > 1) {
                Text message = new Text(messageArr[0] + " : " + messageArr[1]);
                message.getStyleClass().add("messages Text");
                messagesBox.getChildren().add(message);
            }
        } else if (s.contains("SCORES")) {
            s = s.replace("SCORES ", "");
            String[] playerScoreLives = s.split("\n");
            this.multiplayerScores.clear();
            for (String item: playerScoreLives) {
                String[] stats = item.split(":");
                var entry = new Pair<String, Integer>(stats[0], Integer.parseInt(stats[1]));
                this.multiplayerScores.add(entry);
            }
        } else if(s.contains("DIE")) { //A player has lost or left
            s = s.replace("DIE ", "");
            endUser(s);
        } else if(s.contains(("BOARD"))) { //A representation of a player's GameBoard
            s = s.replace("BOARD ", "");
            updatePlayerBoard(s);
        }
    }

    public void initialisePlayerBoards() {
        playerToGameBoard = new HashMap<>();
        for (String player : players) {
            GameBoard gameBoard = new GameBoard(5, 5, 75, 75);
            Text name = new Text(player);
            name.getStyleClass().add("heading");
            name.setTextAlignment(TextAlignment.CENTER);
            boardSideBar.getChildren().addAll(name, gameBoard);
            playerToGameBoard.put(player, gameBoard);
        }
        mainPane.setLeft(boardSideBar);
        boardSideBar.setAlignment(Pos.CENTER_LEFT);
        boardSideBar.setMaxHeight(this.gameWindow.getHeight());
    }

    public void updatePlayerBoard(String board) {
        String player = board.split(":")[0];
        String[] values = board.split(":")[1].split(" ");
        if(players.contains(player)) {
            GameBoard gameBoard = playerToGameBoard.get(player);
            int i = 0;
            for (int x = 0; x < this.game.getCols(); x++) {
                for (int y = 0; y < this.game.getRows(); y++) {
                    gameBoard.getGrid().set(x, y, Integer.parseInt(values[i]));
                    i++;
                }
            }
        }
    }

}