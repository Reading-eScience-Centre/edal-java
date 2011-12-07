package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;

/**
 * Immutable implementation of {@link GridCoordinates}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public class GridCoordinates2DImpl implements GridCoordinates2D {

    private final int xIndex;
    private final int yIndex;

    /**
     * Creates a new GridCoordinatesImpl with the given coordinates.
     * 
     * @param xIndex
     *            The first coordinate
     * @param otherCoords
     *            The remaining coordinates
     */
    public GridCoordinates2DImpl(int xIndex, int yIndex) {
        this.xIndex = xIndex;
        this.yIndex = yIndex;
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
        if (coords == null)
            throw new NullPointerException();
        if (coords.length != 2)
            throw new IllegalArgumentException("Grid co-ordinates must have 2 dimensions");
        xIndex = coords[0];
        yIndex = coords[1];
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
    public int compareTo(GridCoordinates2D other) {
        return GridCoordinatesComparator.INSTANCE.compare(this, other);
    }

    /**
     * <p>
     * Returns a new {@link GridCoordinates2DImpl} object that is a copy of this
     * object. There is generally no need to call this method as
     * {@link GridCoordinates2DImpl} objects are immutable and can be reused
     * freely.
     * </p>
     * <p>
     * Note that GeoAPI should not have specified that {@code
     * org.opengis.coverage.grid.GridCoordinates} extends {@code
     * org.opengis.util.Cloneable}: this should have been left as a mixin for
     * concrete implementations.
     * </p>
     * 
     * @return a new {@link GridCoordinates2DImpl} object that is a copy of this
     *         object.
     * @see #convert(org.opengis.coverage.grid.GridCoordinates)
     */
    @Override
    public Object clone() {
        return new GridCoordinates2DImpl(xIndex,yIndex);
    }

    @Override
    public int hashCode() {
        return (xIndex*yIndex) % Integer.MAX_VALUE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GridCoordinates2D))
            return false;
        GridCoordinates2D other = (GridCoordinates2D) obj;
        return (getXIndex() == other.getXIndex() && getYIndex() == other.getYIndex());
    }

    @Override
    public String toString() {
        return xIndex+","+yIndex;
    }

    @Override
    public int getXIndex() {
        return xIndex;
    }

    @Override
    public int getYIndex() {
        return yIndex;
    }
}
