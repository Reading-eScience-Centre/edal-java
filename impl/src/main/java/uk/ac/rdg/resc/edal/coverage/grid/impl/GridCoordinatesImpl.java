package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.Arrays;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;

/**
 * Immutable implementation of {@link GridCoordinates}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public final class GridCoordinatesImpl implements GridCoordinates {

    /** 1-D immutable GridCoordinates with a single coordinate value of zero */
    public static final GridCoordinatesImpl ZERO_1D = new GridCoordinatesImpl(0);

    /** 2-D immutable GridCoordinates with both values zero */
    public static final GridCoordinatesImpl ZERO_2D = new GridCoordinatesImpl(0, 0);

    /** 3-D immutable GridCoordinates with all values zero */
    public static final GridCoordinatesImpl ZERO_3D = new GridCoordinatesImpl(0, 0, 0);

    /** 4-D immutable GridCoordinates with all values zero */
    public static final GridCoordinatesImpl ZERO_4D = new GridCoordinatesImpl(0, 0, 0, 0);

    private final int[] coords;

    /**
     * Creates a new GridCoordinatesImpl with the given coordinates.
     * 
     * @param coord1
     *            The first coordinate
     * @param otherCoords
     *            The remaining coordinates
     */
    public GridCoordinatesImpl(int coord1, int... otherCoords) {
        int coordsLength = 1;
        if (otherCoords != null)
            coordsLength += otherCoords.length;
        coords = new int[coordsLength];
        coords[0] = coord1;
        if (otherCoords != null) {
            for (int i = 0; i < otherCoords.length; i++) {
                coords[i + 1] = otherCoords[i];
            }
        }
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
    public GridCoordinatesImpl(int[] coords) {
        if (coords == null)
            throw new NullPointerException();
        if (coords.length == 0)
            throw new IllegalArgumentException("Zero-length coordinates array");
        this.coords = coords.clone(); // Copy of coords to preserve immutability
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
     *            The GridCoordinates to convert.
     * @return a GridCoordinatesImpl object containing the same information as
     *         the provided GridCoordinates object.
     * @see #clone()
     * @throws IllegalArgumentException
     *             if the given GridCoordinates object has no dimensions.
     */
    public static GridCoordinatesImpl convert(org.opengis.coverage.grid.GridCoordinates gridCoordinates) {
        if (gridCoordinates instanceof GridCoordinatesImpl) {
            return (GridCoordinatesImpl) gridCoordinates;
        }
        return new GridCoordinatesImpl(gridCoordinates.getCoordinateValues());
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
    public static GridCoordinatesImpl zero(int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension cannot be less than or equal to zero");
        }
        // We reuse objects where we can as they are immutable
        if (dimension == 1)
            return ZERO_1D;
        if (dimension == 2)
            return ZERO_2D;
        if (dimension == 3)
            return ZERO_3D;
        if (dimension == 4)
            return ZERO_4D;
        // Create a new object with the required number of dimensions
        return new GridCoordinatesImpl(new int[dimension]);
    }

    @Override
    public int getDimension() {
        return coords.length;
    }

    @Override
    public int[] getCoordinateValues() {
        // Returns a copy to preserve immutability
        return coords.clone();
    }

    @Override
    public int getCoordinateValue(int dimension) {
        if (dimension < 0 || dimension >= coords.length) {
            String msg = String.format("Attempt to access element at index %d in array of length %d", dimension,
                    coords.length);
            throw new IndexOutOfBoundsException(msg);
        }
        return coords[dimension];
    }

    /**
     * Throws {@link UnsupportedOperationException}: instances of this class are
     * immutable.
     */
    @Override
    public void setCoordinateValue(int dimension, int value) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int compareTo(GridCoordinates other) {
        return GridCoordinatesComparator.INSTANCE.compare(this, other);
    }

    /**
     * <p>
     * Returns a new {@link GridCoordinatesImpl} object that is a copy of this
     * object. There is generally no need to call this method as
     * {@link GridCoordinatesImpl} objects are immutable and can be reused
     * freely.
     * </p>
     * <p>
     * Note that GeoAPI should not have specified that {@code
     * org.opengis.coverage.grid.GridCoordinates} extends {@code
     * org.opengis.util.Cloneable}: this should have been left as a mixin for
     * concrete implementations.
     * </p>
     * 
     * @return a new {@link GridCoordinatesImpl} object that is a copy of this
     *         object.
     * @see #convert(org.opengis.coverage.grid.GridCoordinates)
     */
    @Override
    public Object clone() {
        return new GridCoordinatesImpl(coords);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coords);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GridCoordinates))
            return false;
        GridCoordinates other = (GridCoordinates) obj;
        return Arrays.equals(getCoordinateValues(), other.getCoordinateValues());
    }

    @Override
    public String toString() {
        return Arrays.toString(coords);
    }
}
