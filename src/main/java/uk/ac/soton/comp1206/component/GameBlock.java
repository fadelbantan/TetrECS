package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static javafx.scene.paint.Color.web;

/**
 * The Visual User Interface component representing a single block in the grid.

 * Extends Canvas and is responsible for drawing itself.

 * Displays an empty square (when the value is 0) or a coloured square depending on value.

 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    private AnimationTimer animationTimer;

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    private Boolean center = false;

    private boolean hover = false;

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);

    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        } if (this.center) {
            // Paint center dot
            var gc = getGraphicsContext2D();
            gc.setStroke(Color.GREY);
            gc.fillOval(width/4, height/4, width/2, height/2);
        }
        // If hover is true, then paint hover effect
        if (this.hover) {
            var gc = getGraphicsContext2D();

            gc.setFill(Color.rgb(204, 204, 204, 0.4));
            gc.fillRect(0, 0, width, height);
        }

    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(web("BLACK", 0.5));
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.GREY);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);

        // Creates 3D effect on piece
        gc.setFill(Color.rgb(59, 59, 59, 0.2));
        gc.fillPolygon(new double[]{0, 0, width}, new double[]{0, height, height}, 3);
        gc.setFill(Color.rgb(161, 161, 161, 0.3));
        gc.fillRect(0, 0, 3, height);
        gc.setFill(Color.rgb(255, 255, 255, 0.3));
        gc.fillRect(0, 0, width, 3);

    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    public void paintCursor() {
        var gc = getGraphicsContext2D();
        //Border
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0,0,width,height);
        if(value.get() == 0) {
            gc.setFill(Color.WHITE.deriveColor(0,0,1,0.7));
            gc.fillRect(0,0, width, height);
        }
    }

    public void resetCursor() {
        var gc = getGraphicsContext2D();
        //Resets Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
        if(value.get() == 0) {
            paintEmpty();
        }
    }

    public void center() {
        this.center = true;
        paint();
    }

    /**
     * Hover effect
     * @param hover true if mouse is over board
     */
    public void hover(boolean hover) {
        this.hover = hover;
        paint();
    }


    public void fadeOut() {
        animationTimer = new myAnimationTimer();
        animationTimer.start();
    }

    private class myAnimationTimer extends AnimationTimer {
        double opacityFadeOut = 1;

        @Override
        public void handle(long l) {
            {
                paintEmpty();
                var gc = getGraphicsContext2D();
                opacityFadeOut -= 0.05;
                if (opacityFadeOut <= 0.0) {
                    stop();
                    animationTimer = null;
                    logger.info("Animation Stopped");
                }
                gc.setFill(Color.WHITE.deriveColor(0,0,1,opacityFadeOut));
                gc.fillRect(0,0,width,height);            }
        }
    }

}
