package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;
import uk.ac.rdg.resc.edal.util.Extents;

public final class GridExtentImpl implements GridExtent {

    private final GridCoordinatesImpl low;
    private final GridCoordinatesImpl high;
    // These are calculated from the GridCoordinates upon construction
    private transient final long size;

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
    public GridExtentImpl(GridCoordinates2D low, GridCoordinates2D high) {
        if (high.getXIndex() < low.getXIndex() || high.getYIndex() < low.getYIndex()) {
            String msg = String.format("A high coordinate is lower" + " than a low coordinate");
            throw new IllegalArgumentException(msg);
        }
        // We ensure that the internal GridCoordinates objects are instances of
        // GridCoordinatesImpl to ensure that they are immutable
        this.low = GridCoordinatesImpl.convert(low);
        this.high = GridCoordinatesImpl.convert(high);

        /*
         * int * int -> int, EVEN WHEN RESULT IS TOO BIG
         * 
         * Therefore we must cast (at least one of the values) to long
         */
        size = (long) getXSpan() * (long) getYSpan();
    }
    
    public GridExtentImpl(Extent<Integer> xExtent, Extent<Integer> yExtent) {
        this(new GridCoordinatesImpl(xExtent.getLow(), yExtent.getLow()),
                new GridCoordinatesImpl(xExtent.getHigh(),yExtent.getHigh()));
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
    public GridExtentImpl(GridCoordinates2D high) {
        this(GridCoordinatesImpl.zero(), high);
    }

    /**
     * Creates a new GridExtent with the given high coordinates with all low
     * coordinates equal to zero.
     * 
     * @param highCoordX
     *            The high coordinate of the x-axis
     * @param highCoordY
     *            The high coordinate of the y-axis
     * @throws IllegalArgumentException
     *             if any of the high coordinates is less than zero.
     */
    public GridExtentImpl(int highCoordX, int highCoordY) {
        this(new GridCoordinatesImpl(highCoordX, highCoordY));
    }

    /**
     * Returns true if this envelope contains the given coordinates.
     * 
     * @param coordinates
     *            The coordinates to test
     * @return true if this envelope contains the given coordinates.
     */
    @Override
    public boolean contains(GridCoordinates2D coords) {
        return contains(coords.getXIndex(), coords.getYIndex());
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
    public boolean contains(int xIndex, int yIndex) {
        return (xIndex >= getLow().getXIndex() && xIndex <= getHigh().getXIndex() &&
                yIndex >= getLow().getYIndex() && yIndex <= getHigh().getYIndex());
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
    
    private int getXSpan(){
        return getHigh().getXIndex() - getLow().getXIndex() + 1;
    }
    
    private int getYSpan(){
        return getHigh().getYIndex() - getLow().getYIndex() + 1;
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

    @Override
    public boolean isEmpty() {
        return (low == null && high == null);
    }

    @Override
    public Extent<Integer> getXExtent() {
        return Extents.newExtent(low.getXIndex(), high.getXIndex());
    }

    @Override
    public Extent<Integer> getYExtent() {
        return Extents.newExtent(low.getYIndex(), high.getYIndex());
    }

    @Override
    public long size() {
        return size;
    }
}
