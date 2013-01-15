package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;

public interface Palette {
    /**
     * This returns a colour, given a value between 0 and 1
     * 
     * @param value
     *            The value to translate to a colour, between 0 and 1 (should
     *            throw an exception if out of range)
     * @return A {@link Color}
     */
    public Color getColor(float value);
}
