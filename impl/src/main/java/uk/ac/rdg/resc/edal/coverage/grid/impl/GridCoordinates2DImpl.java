package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.Arrays;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;

/**
 * Immutable implementation of {@link GridCoordinates2D}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class GridCoordinates2DImpl extends GridCoordinatesImpl implements GridCoordinates2D {

    /**
     * Creates a new GridCoordinates2DImpl with the given coordinates.
     * 
     * @param xIndex
     *            The first coordinate
     * @param otherCoords
     *            The remaining coordinates
     */
    public GridCoordinates2DImpl(int xIndex, int yIndex) {
        super(xIndex, yIndex);
    }

    /**
     * Create a new GridCoordinates2DImpl from an existing
     * {@link GridCoordinates} object, throwing an exception if it is not
     * compatible
     * 
     * @param coords
     *            The {@link GridCoordinates} object to convert
     */
    public GridCoordinates2DImpl(GridCoordinates coords) {
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
    public GridCoordinates2DImpl(int[] coords) {
        super(checkCoords(coords));
    }

    private static int[] checkCoords(int[] coords) {
        if (coords == null)
            throw new NullPointerException();
        if (coords.length != 2)
            throw new IllegalArgumentException("Grid co-ordinates must have 2 dimensions");
        return coords;
    }

    /**
     * <p>
     * Creates a new GridCoordinatesImpl from the given GridCoordinates object.
     * This may be used to convert a GridCoordinates object of unknown type to
     * one of this type, perhaps to make an object that is guaranteed immutable.
     * </p>
     * <p>
     * If {@code gridCoordinates} is already an instance of GridCoordinatesImpl,
     * this method simply returns it: no new objects are created. This method is
     * therefore different from {@link #clone()}, which <i>always</i> returns a
     * new object.
     * </p>
     * 
     * @param gridCoordinates
     *            The GridCoordinates2D to convert.
     * @return a GridCoordinatesImpl object containing the same information as
     *         the provided GridCoordinates2D object.
     * @see #clone()
     */
    public static GridCoordinates2DImpl convert(GridCoordinates2D gridCoordinates) {
        if (gridCoordinates instanceof GridCoordinates2DImpl) {
            return (GridCoordinates2DImpl) gridCoordinates;
        }
        return new GridCoordinates2DImpl(gridCoordinates.getXIndex(), gridCoordinates.getYIndex());
    }

    /**
     * Returns a GridCoordinatesImpl object in which all coordinates are zero.
     * 
     * @param dimension
     *            the required number of dimensions
     * @return a GridCoordinatesImpl object in which all coordinates are zero.
     * @throws IllegalArgumentException
     *             if {@code dimension <= 0}
     */
    public static GridCoordinates2DImpl zero() {
        return new GridCoordinates2DImpl(new int[2]);
    }

    @Override
    public int hashCode() {
        // I think this follows the specification of GeoAPI.GridCoordinates
        return Arrays.hashCode(new int[] { getXIndex(), getYIndex() });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GridCoordinates2D)) {
            if (!(obj instanceof GridCoordinates))
                return false;
            if (obj instanceof GridCoordinates) {
                GridCoordinates gCoords = (GridCoordinates) obj;
                return gCoords.getNDim() == 2 && getXIndex() == gCoords.getIndex(0)
                        && getYIndex() == gCoords.getIndex(1);
            }
        }
        GridCoordinates2D other = (GridCoordinates2D) obj;
        return (getXIndex() == other.getXIndex() && getYIndex() == other.getYIndex());
    }

    @Override
    public String toString() {
        return getXIndex() + "," + getYIndex();
    }

    @Override
    public int getXIndex() {
        return getIndex(0);
    }

    @Override
    public int getYIndex() {
        return getIndex(1);
    }
}