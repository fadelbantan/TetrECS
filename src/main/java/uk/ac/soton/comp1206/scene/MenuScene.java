package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    private Multimedia multimedia = new Multimedia();

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        // Better title
        Image image = new Image((getClass().getResource("/images/TetrECS.png").toExternalForm()));
        ImageView logo = new ImageView(image);

        logo.setFitHeight(130);
        logo.setPreserveRatio(true);
        mainPane.setCenter(logo);

        // Better title animation
        RotateTransition rotate = new RotateTransition(Duration.millis(2000), logo);
        rotate.setToAngle(5);
        rotate.setFromAngle(-5);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.play();

        Multimedia.playMusic("menu.mp3");

        // Menu items
        var menu = new VBox(10);
        menu.setPadding(new Insets(15));
        menu.setAlignment(Pos.CENTER);
        mainPane.setBottom(menu);

        var local = new Text("Local");
        local.getStyleClass().add("menuItem");
        menu.getChildren().add(local);
        local.setOnMouseClicked(e -> gameWindow.startChallenge());

        var online = new Text("Online");
        online.getStyleClass().add("menuItem");
        menu.getChildren().add(online);

        var instructions = new Text("Instructions");
        instructions.getStyleClass().add("menuItem");
        menu.getChildren().add(instructions);

        var quit = new Text("Quit");
        quit.getStyleClass().add("menuItem");
        quit.setOnMouseClicked((e) -> App.getInstance().shutdown());
        menu.getChildren().add(quit);

//        local.setOnAction(this::startGame);
//        online.setOnAction(this::startMultiplayer);
//        instructions.setOnAction(this::startInstructions);


    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        Multimedia.playAudio("menu.mp3");

        // Exit when escaped is pressed
        scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ESCAPE) {
                logger.info("Escape Pressed");
                System.exit(0);
            }
        });
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
        Multimedia.stopBackground();
    }
    private void startMultiplayer(ActionEvent event) {

    }

    private void startInstructions(ActionEvent event) {
        gameWindow.startInstructionsScene();
        Multimedia.stopBackground();
    }
}
