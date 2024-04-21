package uk.ac.soton.comp1206.ui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    public static MediaPlayer musicPlayer;
    public static MediaPlayer audioPlayer;

    /**
     * Play background music
     * @param music music name
     */
    public static void playMusic(String music) {
        String toPlay = Multimedia.class.getResource("resources/music/" + music).toExternalForm();
        musicPlayer = new MediaPlayer(new Media(toPlay));
        musicPlayer.play();
        logger.info("Music played: " + music);
    }

    /**
     * Play audio effects
     * @param sound sound name
     */
    public static void playAudio(String sound) {
        String toPlay = Objects.requireNonNull(Multimedia.class.getResource("resources/sounds/" + sound)).toExternalForm();
        audioPlayer = new MediaPlayer(new Media(toPlay));
        audioPlayer.play();
        logger.info("Audio played: " + sound);
    }
}
