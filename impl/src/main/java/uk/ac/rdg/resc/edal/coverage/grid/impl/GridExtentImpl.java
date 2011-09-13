package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;

public final class GridExtentImpl implements GridExtent {

    private final GridCoordinatesImpl low;
    private final GridCoordinatesImpl high;
    private final int dimensionality;
    // These are calculated from the GridCoordinates upon construction
    private transient final int[] combos;
    private transient final int size;

    /**
     * Creates a new GridExtent with the given low and high coordinates. Note
     * that both sets of coordinates are <b>inclusive</b>. The grid coordinate
     * information is copied to internal data structures so modifying these
     * parameters externally has no effect on this object.
     * 
     * @param low
     *            the low coordinates of the grid
     * @param high
     *            the high coordinates of the grid
     * @throws IllegalArgumentException
     *             if {@code low.getDimension() != high.getDimension()} or if
     *             any of the high coordinates is lower than its corresponding
     *             low coordinate.
     */
    public GridExtentImpl(GridCoordinates low, GridCoordinates high) {
        if (low.getDimension() != high.getDimension()) {
            throw new IllegalArgumentException("Dimensions of low and high " + "GridCoordinates must be equal");
        }
        dimensionality = low.getDimension();
        for (int i = 0; i < dimensionality; i++) {
            if (high.getCoordinateValue(i) < low.getCoordinateValue(i)) {
                String msg = String.format("High coordinate at index %d is lower" + " than the low coordinate", i);
                throw new IllegalArgumentException(msg);
            }
        }
        // We ensure that the internal GridCoordinates objects are instances of
        // GridCoordinatesImpl to ensure that they are immutable
        this.low = GridCoordinatesImpl.convert(low);
        this.high = GridCoordinatesImpl.convert(high);

        // Calculate the number of combinations of coordinates for each
        // dimension. We need this for indexOf() and getCoordinates(), which may
        // be called many times, so this pre-calculation is sensible.
        // We calculate the number of coordinates in this grid (the size) at the
        // same time.
        combos = new int[dimensionality];
        combos[dimensionality - 1] = 1;
        int n = getSpan(dimensionality - 1);
        for (int j = dimensionality - 2; j >= 0; j--) {
            combos[j] = combos[j + 1] * getSpan(j + 1);
            n *= getSpan(j);
        }
        size = n;
    }

    /**
     * Creates a new GridExtent with the given high coordinates with all low
     * coordinates equal to zero.
     * 
     * @param high
     *            the high coordinates of the grid
     * @throws IllegalArgumentException
     *             if any of the high coordinates is less than zero.
     */
    public GridExtentImpl(GridCoordinates high) {
        this(GridCoordinatesImpl.zero(high.getDimension()), high);
    }

    /**
     * Creates a new GridExtent with the given high coordinates with all low
     * coordinates equal to zero.
     * 
     * @param highCoord1
     *            The first high coordinate
     * @param otherHighCoords
     *            The remaining high coordinates
     * @throws IllegalArgumentException
     *             if any of the high coordinates is less than zero.
     */
    public GridExtentImpl(int highCoord1, int... otherHighCoords) {
        this(new GridCoordinatesImpl(highCoord1, otherHighCoords));
    }

    /**
     * Returns true if this envelope contains the given coordinates.
     * 
     * @param coordinates
     *            The coordinates to test
     * @return true if this envelope contains the given coordinates.
     */
    @Override
    public boolean contains(GridCoordinates coords) {
        return contains(coords.getCoordinateValues());
    }

    /**
     * Returns true if this envelope contains the given coordinates
     * 
     * @param coords
     *            The coordinates to test
     * @throws IllegalArgumentException
     *             if the number of coordinates given does not match the
     *             dimensionality of the grid
     * @return true if this envelope contains the given coordinates
     */
    public boolean contains(int... coords) {
        if (coords == null)
            throw new NullPointerException();
        if (coords.length != dimensionality) {
            throw new IllegalArgumentException("coords.length should be " + dimensionality);
        }

        for (int i = 0; i < coords.length; i++) {
            if (coords[i] < getLow().getCoordinateValue(i) || coords[i] > getHigh().getCoordinateValue(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Creates a new GridExtentImpl from the given GridExtent object. This
     * may be used to convert a GridExtent object of unknown type to one of
     * this type, perhaps to make an object that is guaranteed immutable.
     * </p>
     * <p>
     * If {@code env} is already an instance of GridExtentImpl, this method
     * simply returns it: no new objects are created.
     * </p>
     * 
     * @param env
     *            The GridExtent to copy.
     * @return a GridExtentImpl object containing the same information as the
     *         provided GridExtent object.
     */
    public static GridExtentImpl convert(GridExtent env) {
        if (env instanceof GridExtentImpl)
            return (GridExtentImpl) env;
        return new GridExtentImpl(env.getLow(), env.getHigh());
    }

    @Override
    public int getDimension() {
        return dimensionality;
    }

    /**
     * Returns the minimal coordinate values for all grid points within the
     * grid. The returned object is immutable.
     * 
     * @return The minimal coordinate values for all grid points,
     *         <b>inclusive</b>.
     */
    @Override
    public GridCoordinatesImpl getLow() {
        return low;
    }

    /**
     * Returns the maximal coordinate values for all grid points within the
     * grid. The returned object is immutable.
     * 
     * @return The maximal coordinate values for all grid points,
     *         <b>inclusive</b>.
     */
    @Override
    public GridCoordinatesImpl getHigh() {
        return high;
    }

    public int getLow(int dimension) {
        checkIndex(dimension);
        return low.getCoordinateValue(dimension);
    }

    public int getHigh(int dimension) {
        checkIndex(dimension);
        return high.getCoordinateValue(dimension);
    }

    public int getSpan(int dimension) {
        checkIndex(dimension);
        return getHigh(dimension) - getLow(dimension) + 1;
    }

    private void checkIndex(int dimension) {
        if (dimension < 0 || dimension >= dimensionality) {
            String msg = String.format("Attempt to access element at dimension "
                    + "%d in envelope of dimensionality %d", dimension, dimensionality);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    /**
     * <p>
     * Returns the <i>i</i>th set of grid coordinates in this envelope, based
     * upon the ordering defined by
     * {@link org.jcsml.coverage.grid.GridCoordinates#compareTo(org.jcsml.coverage.grid.GridCoordinates)}. 
     * {@code getCoordinates(0)} is therefore equivalent to {@link #getLow()
     * getLow()} and {@code getCoordinates(getSize() - 1)} is equivalent to
     * {@link #getHigh() getHigh()}.
     * </p>
     * <p>
     * The returned {@link GridCoordinates} object will be immutable.
     * </p>
     * <p>
     * This method is the inverse of
     * {@link #indexOf(org.opengis.coverage.grid.GridCoordinates)
     * indexOf(GridCoordinates)}.
     * </p>
     * 
     * @param i
     *            the index through the grid coordinates in this envelope
     * @return the <i>i</i>th set of grid coordinates in this envelope
     * @throws IllegalArgumentException
     *             if {@code i < 0} or {@code i >= getSize()}.
     */
    public GridCoordinatesImpl getCoordinates(int i) {
        if (i < 0 || i >= size) {
            String msg = String.format("The requested index (%d) is outside the" + " range of this envelope", i);
            throw new IllegalArgumentException(msg);
        }
        // Shortcuts for efficiency
        if (i == 0)
            return getLow();
        if (i == size - 1)
            return getHigh();

        // Now we find the coordinate index for each axis
        int[] coords = new int[dimensionality];
        for (int j = 0; j < dimensionality; j++) {
            coords[j] = i / combos[j];
            i %= combos[j];
        }

        // Now we must add the low coordinates to these
        for (int j = 0; j < dimensionality; j++) {
            coords[j] += low.getCoordinateValue(j);
        }

        return new GridCoordinatesImpl(coords);
    }

    private int hashCode = Integer.MAX_VALUE;

    @Override
    public int hashCode() {
        if (hashCode == Integer.MAX_VALUE) {
            hashCode = 17;
            hashCode = 31 * hashCode + low.hashCode();
            hashCode = 31 * hashCode + high.hashCode();
        }
        return hashCode;
    }

    /**
     * Compares for equality only with other {@link GridExtentImpl} objects;
     * {@literal i.e.}, other implementations of the {@link GridExtent}
     * interface are not considered equal to implementations of
     * {@link GridExtentImpl}. (This is because the {@link GridExtent} interface
     * does not defined a contract for equals() or hashCode().)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GridExtentImpl))
            return false;
        GridExtentImpl other = (GridExtentImpl) obj;
        return low.equals(other.low) && high.equals(other.high);
    }

    @Override
    public String toString() {
        return low.toString() + ":" + high.toString();
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return (low == null && high == null);
    }
}
