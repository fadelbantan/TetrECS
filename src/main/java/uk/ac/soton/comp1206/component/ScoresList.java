package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 *The ScoresList class represents a graphical component for displaying scores of players.
 * It extends VBox and provides functionality to render and animate score items.
 */
public class ScoresList extends VBox {
    /**
     * The scores of all players
     */
    protected SimpleListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>();

    /**
     * All Scores of players within the same lobby
     */
    protected ArrayList<String> multiplayerPlayers = new ArrayList<>();

    public ScoresList() {
        this.localScores.addListener(this::updateScores); //Listener detects when there is a change to the listProperty
        localScores.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
    }

    /**
     * Updates the scores based on changes in the observable value
     *
     * @param observableValue The observable value being monitored
     * @param oldVal The old value of the observable list
     * @param newVal The new value of the observable list
     */
    protected void updateScores(ObservableValue<?  extends ObservableList<Pair<String, Integer>>> observableValue, ObservableList<Pair<String, Integer>> oldVal,  ObservableList<Pair<String, Integer>> newVal) {
        this.renderScores(newVal);
    }

    /**
     * renderScores will first clear the VBox, then iterate through the given List, creating a Text item that will
     * display the name of the player and their score, then animating the text item.
     *
     * @param scores List of scores of all players
     */
    protected void renderScores(ObservableList<Pair<String, Integer>> scores) {
        this.getChildren().clear();
        int x = 1;
        for(Pair pair: scores) {
            Text scoreItem = new Text(pair.getKey() + " - " + pair.getValue());
            if(multiplayerPlayers.contains(pair.getKey())) {
                scoreItem.getStyleClass().add("scoreListStrike");
            } else {
                scoreItem.getStyleClass().add("scoreList");
            }
            this.getChildren().add(scoreItem);
            this.reveal(scoreItem);
            x++;
            // This to only show the top 10 scores
            if(x==11){
                break;
            }
        }
    }

    /**
     * Animates the given text item to fade in once, and then remain shown to the user
     *
     * @param item Text to be animated
     */
    protected void reveal(Text item) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), item);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setCycleCount(1);
        fadeTransition.setAutoReverse(false);
        fadeTransition.play();
    }

    /**
     * Returns the SimpleListProperty so that it can be bound to
     *
     * @return localScores
     */
    public ListProperty<Pair<String, Integer>> listProperty() {
        return this.localScores;
    }

    /**
     * Takes the given string and searches for a pair that contains that string, then changes style class of the
     * Text item to have a strikethrough.
     *
     * @param item The string to be searched for in the pairs.
     */
    public void strikeThrough(String item) {
        int x = 0;
        this.multiplayerPlayers.add(item);
        for (Pair pair: localScores) {
            if(pair.getKey().equals(item)){
                this.getChildren().get(x).getStyleClass().add("scoreListStrike");
            } else {
                x++;
            }
        }
    }
}
