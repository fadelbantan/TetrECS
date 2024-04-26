package uk.ac.soton.comp1206.ui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    public static MediaPlayer backgroundPlayer;
    public static MediaPlayer audioPlayer;

    /**
     * Play background music
     * @param music music name
     */
    public void playMusic(String music) {
        String toPlay = Multimedia.class.getResource("/music/" + music).toExternalForm();
        try {
            Media play = new Media(toPlay);
            backgroundPlayer = new MediaPlayer(play);

            backgroundPlayer.setAutoPlay(true);
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            Duration end = play.getDuration();
            backgroundPlayer.setStartTime(Duration.seconds(0));
            backgroundPlayer.setStopTime(end);

            backgroundPlayer.play();
            logger.info("Playing background music: " + music);
        } catch(Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }

    /**
     * Play audio effects
     * @param sound sound name
     */
    public void playAudio(String sound) {
        String toPlay = Objects.requireNonNull(Multimedia.class.getResource("resources/sounds/" + sound)).toExternalForm();
        try {
            Media play = new Media(toPlay);
            audioPlayer = new MediaPlayer(play);
            audioPlayer.play();
            logger.info("Playing media sound" + sound);

        } catch(Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }

    /**
     * Stopping background music
     */
    public void stopBackground() {
        backgroundPlayer.stop();
        logger.info("Background Music Stopped");
    }

}
