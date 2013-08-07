package uk.ac.rdg.resc.edal.util;

/**
 * Class representing a pair of integer co-ordinates. This is used in preference
 * to int[] so that the order does not cause issues
 * 
 * @author Guy
 */
public class GridCoordinates2D {
    int[] coords;

    public GridCoordinates2D(int x, int y) {
        coords = new int[] { x, y };
    }

    public int getX() {
        return coords[0];
    }

    public int getY() {
        return coords[1];
    }
}
