package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionsScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    private GridPane gridPane = new GridPane();

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instructions Scene");
    }
    @Override
    public void initialise() {
        //Escape Key Event
        scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ESCAPE) {
                gameWindow.startMenu();
                logger.info("Escape Pressed");
            }
        });
    }
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());
        this.scene = gameWindow.getScene();

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Titles
        var instructions = new Text("Instructions");
        mainPane.getChildren().add(instructions);
        instructions.getStyleClass().add("heading");
        StackPane.setAlignment(instructions, Pos.CENTER);
        instructions.setLayoutY(20);
        instructions.setLayoutX(400 - instructions.getLayoutBounds().getWidth());

        var pieces = new Text("Pieces");
        mainPane.getChildren().add(pieces);
        pieces.getStyleClass().add("heading");
        StackPane.setAlignment(pieces, Pos.CENTER);
        pieces.setLayoutY(425);
        pieces.setLayoutX(400 - pieces.getLayoutBounds().getWidth());

        //Instructions
        Image instructionImage = new Image(MenuScene.class.getResource("/images/Instructions.png").toExternalForm());
        ImageView imageView = new ImageView(instructionImage);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600);
        imageView.setX(100);
        imageView.setY(50);
        mainPane.getChildren().add(imageView);

        //Pieces
        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(100, gameWindow.getWidth());
        for(int x=0; x<15; x++) {
            GameBoard gameBoard = new GameBoard(3,3,50,50);
            GamePiece gamePiece = GamePiece.createPiece(x);
            gameBoard.pieceToDisplay(gamePiece);
            if(x < 8) {
                gridPane.add(gameBoard, x, 0);
            } else {
                gridPane.add(gameBoard, x - 8, 1);
            }
        }
        mainPane.getChildren().add(gridPane);
        gridPane.setLayoutY(450);
        gridPane.setLayoutX(160);
        gridPane.setVgap(10);
        gridPane.setHgap(10);
    }

    /**
     * Creates the 15 game pieces' grids
     */
//    public void createDynamicPieces() {
//        int i = 0;
//        while (i <= 14) {
//            int a = 0;
//            while (a <= 4) {
//                int b = 0;
//                while (b <= 2) {
//                    gridPane.add(new PieceBoard(GamePiece.createPiece(i), 50, 50), a, b);
//                    i++;
//                    b++;
//                }
//                a++;
//            }
//        }
//    }
}
