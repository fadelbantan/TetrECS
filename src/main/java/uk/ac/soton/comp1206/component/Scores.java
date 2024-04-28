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

public class Scores extends VBox {
    protected SimpleListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>();
    protected ArrayList<String> multiplayerPlayers = new ArrayList<>();

    public Scores() {
        this.localScores.addListener(this::updateScores);
        localScores.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
    }

    protected void updateScores(ObservableValue<?  extends ObservableList<Pair<String, Integer>>> observableValue, ObservableList<Pair<String, Integer>> oldVal, ObservableList<Pair<String, Integer>> newVal) {
        this.renderScores(newVal);
    }


    protected void renderScores(ObservableList<Pair<String, Integer>> scores) {
        this.getChildren().clear();
        int x = 1;
        for(Pair pair: scores) {
            Text scoreItem = new Text(pair.getKey() + " - " + pair.getValue());
            if (multiplayerPlayers.contains(pair.getKey())) {
                scoreItem.getStyleClass().add("scoresStrike");
            } else {
                scoreItem.getStyleClass().add("scores");
            }

            this.getChildren().add(scoreItem);
            this.reveal(scoreItem);
            x++;
            // Only shows the top 10 scores
            if(x==11){
                break;
            }
        }
    }

    protected void reveal(Text item) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), item);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setCycleCount(1);
        fadeTransition.setAutoReverse(false);
        fadeTransition.play();
    }

    public ListProperty<Pair<String, Integer>> listProperty() {
        return this.localScores;
    }

    public void strikeThrough(String item) {
        int x = 0;
        this.multiplayerPlayers.add(item);
        for (Pair pair: localScores) {
            if (pair.getKey().equals(item)) {
                this.getChildren().get(x).getStyleClass().add("scoreStrike");
            } else {
                x++;
            }
        }
    }
}
