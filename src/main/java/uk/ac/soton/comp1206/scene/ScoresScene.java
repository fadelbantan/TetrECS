package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Scores;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class ScoresScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    private Multimedia multimedia = new Multimedia();

    protected Game gameState;

    protected int score;

    protected String name;

    protected SimpleListProperty<Pair<String, Integer>> localScoreList = new SimpleListProperty<>();

    protected SimpleListProperty<Pair<String, Integer>> remoteScoresList = new SimpleListProperty<>();

    protected SimpleListProperty<Pair<String, Integer>> multiplayerScores;
    protected Communicator communicator;

    protected boolean isMultiplayer = false;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        gameState = game;
        score = game.scoreProperty().get();
        this.localScoreList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        logger.info("Creating Scores Scene");
        communicator = gameWindow.getCommunicator();

    }


    /**
     * TODO HEREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
     * Create a new scene, passing in the GameWindow the scene will be displayed in, and passing in multiplayer scores
     * which will displayed as a scorelist
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow, Game game, SimpleListProperty<Pair<String, Integer>> scores) {
        super(gameWindow);
        gameState = game;
        score = game.scoreProperty().get(); //sets the score that the player achieved
        //Sets the localscorelist to the scores in multiplayer
        this.localScoreList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        this.remoteScoresList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        multiplayerScores = scores;
        logger.info("Creating Scores Scene");
        communicator = gameWindow.getCommunicator();
        isMultiplayer = true;
    }

    @Override
    public void initialise() {
        multimedia.playAudio("explode.wav");
        multimedia.playMusic("end.wav");

        if(!isMultiplayer) {
            loadScores();
            addScore(this.name, this.score);
        } else {
            this.localScoreList.addAll(multiplayerScores);
            this.localScoreList.sort((a, b) -> b.getValue() - a.getValue());
        }
        this.scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ESCAPE) {
                multimedia.playAudio("transition.wav");
                gameWindow.startMenu();
                logger.info("Escape Pressed");
                if (isMultiplayer) {
                    communicator.send("PART");
                }
            }
        });
        loadOnlineScores();
        communicator.addListener(message -> Platform.runLater(() -> receiveCommunication(message.trim())));
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var scorePane = new StackPane();
        scorePane.setMaxWidth(gameWindow.getWidth());
        scorePane.setMaxHeight(gameWindow.getHeight());
        scorePane.getStyleClass().add("menu-background");
        root.getChildren().add(scorePane);

        var mainPane = new BorderPane();
        scorePane.getChildren().add(mainPane);

        var scores = new HBox();
        scores.setAlignment(Pos.CENTER);
        scorePane.getChildren().add(scores);

        var localScores = new VBox();
        localScores.setAlignment(Pos.CENTER);
        scores.getChildren().add(localScores);

        var onlineScores = new VBox();
        onlineScores.setAlignment(Pos.CENTER);
        scores.getChildren().add(onlineScores);

        Text highScores = new Text("Game Over - High Scores");
        highScores.setTextAlignment(TextAlignment.CENTER);
        highScores.getStyleClass().add("title");
        scorePane.getChildren().add(highScores);
        StackPane.setAlignment(highScores, Pos.TOP_CENTER);

        var scoreText = new Text("Local Scores");
        scoreText.getStyleClass().add("heading");
        localScores.getChildren().add(scoreText);
        scoreText.setTranslateX(300);

        var scoresList = new Scores();
        localScores.getChildren().add(scoresList);
        scoresList.setAlignment(Pos.CENTER);
        scoresList.setTranslateX(300);
        this.localScoreList.bind(scoresList.listProperty());

        var onlineScoreText = new Text("Online Scores");
        onlineScoreText.getStyleClass().add("heading");
        onlineScores.getChildren().add(onlineScoreText);
        onlineScoreText.setTranslateX(-300);

        var onlineScoresList = new Scores();
        onlineScores.getChildren().add(onlineScoresList);
        onlineScoresList.setAlignment(Pos.CENTER);
        onlineScoresList.setTranslateX(-300);
        this.remoteScoresList.bind(onlineScoresList.listProperty());

        if(!isMultiplayer) {
            var nameDialog = new TextInputDialog();
            nameDialog.setTitle("Score Input");
            nameDialog.setContentText("Enter Name To Add to Leaderboard");
            Optional<String> result = nameDialog.showAndWait();
            this.name = result.orElse("Anon");
        }

        var exit = new Button("Exit To Main Menu");
        exit.setBackground(null);
        scorePane.getChildren().add(exit);
        exit.setOnAction(this::startMenu);
        exit.setAlignment(Pos.CENTER);
        exit.setTranslateY(200);
        exit.getStyleClass().add("menuItem");

        exit.hoverProperty().addListener((ov, oldValue, newValue) -> {
            if (newValue) {
                exit.setStyle("-fx-text-fill: yellow");
            } else {
                exit.setStyle("-fx-text-fill: white");
            }
        });
        exit.setStyle("-fx-text-fill: white");
    }

    protected void loadScores() {
        File file = new File("scores.txt");
        try {
            var fileCreate = file.createNewFile();
            if (fileCreate) {
                writeScores();
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                Scanner scanner = new Scanner(reader);
                while (scanner.hasNext()) {
                    String[] nameScore = scanner.next().split(":");
                    var entry = new Pair<String, Integer>(nameScore[0], Integer.parseInt(nameScore[1]));
                    this.localScoreList.add(entry);
                }
                scanner.close();
            }
        } catch (Exception e) {
            logger.error("Unable to complete file making");
            e.printStackTrace();
        }
    }

    private void writeScores() {
        ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
        File file = new File("scores.txt");
        try {
            file.createNewFile();

            scores.add(new Pair<>("Fadel", 1000));
            scores.add(new Pair<>("Fadel", 900));
            scores.add(new Pair<>("Fadel", 750));
            scores.add(new Pair<>("Fadel", 700));
            scores.add(new Pair<>("Fadel", 600));
            scores.add(new Pair<>("Fadel", 500));

            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (Pair pair : scores) {
                String nameScore = pair.getKey() + ":" + pair.getValue();
                localScoreList.add(pair);
                bufferedWriter.write(nameScore);
                bufferedWriter.write("\n");
            }
            bufferedWriter.close();
            fileWriter.close();


        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error writing to file");
        }
    }

    public void addScore(String name, int score) {
        File file = new File("scores.txt");
        try{
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(name + ":" + score);
            bufferedWriter.write("\n");
            bufferedWriter.close();
            fileWriter.close();

            this.localScoreList.add(new Pair<String, Integer>(this.name, this.score));
            this.localScoreList.sort((a, b) -> b.getValue() - a.getValue());
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Unable to add score to text file");
        }
    }


    protected void loadOnlineScores() {
        communicator.send("HISCORES");
    }

    protected void writeOnlineScore() {
        communicator.send("HISCORE " + this.name + ":" + this.score);
    }

    protected void receiveCommunication(String message) {
        if(message.contains("NEWSCORE")) {
            logger.info("Server received highscore");
        } else if (message.contains("HISCORE")) {
            message = message.replace("HISCORES", "");
            String[] pairs = message.split("\n");
            for (String pair : pairs) {
                String[] scoreName = pair.split(":");
                remoteScoresList.add(new Pair<>(scoreName[0], Integer.parseInt(scoreName[1])));
            }
            if(remoteScoresList.get(8).getValue() < this.score) {
                writeOnlineScore();
            }
        }
    }

    protected void startMenu(ActionEvent event) {
        gameWindow.startMenu();
        multimedia.stopBackground();
        multimedia.playAudio("transition.wav");
        if (isMultiplayer) {
            communicator.send("PART");
        }
    }
}
