package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates4D;

/**
 * Immutable instance of a {@link GridCoordinates4D}
 * 
 * @author Guy Griffiths
 * 
 */
public class GridCoordinates4DImpl extends GridCoordinatesImpl implements GridCoordinates4D {

    public GridCoordinates4DImpl(int xIndex, int yIndex, int zIndex, int tIndex) {
        super(xIndex, yIndex, zIndex, tIndex);
    }

    /**
     * Create a new GridCoordinates2DImpl from an existing
     * {@link GridCoordinates} object, throwing an exception if it is not
     * compatible
     * 
     * @param coords
     *            The {@link GridCoordinates} object to convert
     */
    public GridCoordinates4DImpl(GridCoordinates coords) {
        super(checkCoords(coords.getIndices()));
    }

    /**
     * Creates a new GridCoordinatesImpl with the given coordinates.
     * 
     * @param coords
     *            The coordinates of this position.
     * @throws NullPointerException
     *             if {@code coords == null}
     * @throws IllegalArgumentException
     *             if {@code coords.length == 0}.
     */
    public GridCoordinates4DImpl(int[] coords) {
        super(checkCoords(coords));
    }

    private static int[] checkCoords(int[] coords) {
        if (coords == null)
            throw new NullPointerException();
        if (coords.length != 4)
            throw new IllegalArgumentException("This grid co-ordinates must have 4 dimensions");
        return coords;
    }

    @Override
    public int getXIndex() {
        return getIndex(0);
    }

    @Override
    public int getYIndex() {
        return getIndex(1);
    }

    @Override
    public int getZIndex() {
        return getIndex(2);
    }

    @Override
    public int getTIndex() {
        return getIndex(3);
    }
}
