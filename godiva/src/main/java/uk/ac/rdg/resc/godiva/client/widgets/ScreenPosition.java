package uk.ac.rdg.resc.godiva.client.widgets;

import uk.ac.rdg.resc.godiva.client.widgets.DialogBoxWithCloseButton.CentrePosIF;

/**
 * Defines a screen position. Used with {@link CentrePosIF} to define a local
 * centre for {@link DialogBoxWithCloseButton}s
 * 
 * @author Guy Griffiths
 * 
 */
public class ScreenPosition {
    private int x;
    private int y;

    public ScreenPosition(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
