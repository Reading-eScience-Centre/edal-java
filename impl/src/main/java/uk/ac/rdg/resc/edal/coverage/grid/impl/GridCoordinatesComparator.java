package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.Comparator;
import org.opengis.coverage.grid.GridCoordinates;

/**
 * <p>
 * A {@link Comparator} for {@link GridCoordinates} objects that implements the
 * ordering defined in
 * {@link uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates#compareTo(uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates)}
 * . Collections of {@link GridCoordinates} objects that are sorted using this
 * comparator will end up with coordinates sorted such that the last coordinate
 * varies fastest, which is likely to match the order in which corresponding
 * data values are stored (e.g. on disk).
 * </p>
 * <p>
 * This object is stateless and therefore immutable, hence a single object is
 * created that can be reused freely.
 * </p>
 * 
 * @author Jon
 */
public enum GridCoordinatesComparator implements Comparator<GridCoordinates> {

    /** Singleton instance */
    INSTANCE;

    /**
     * <p>
     * Compares two {@link GridCoordinates} objects for order. We define this
     * ordering as follows:
     * </p>
     * <ul>
     * <li>First compare the dimensions of the objects. If they are different,
     * the object with the greater number of dimensions is considered greater.</li>
     * <li>Next compare each individual coordinate from each object, starting
     * with {@link #getCoordinateValue(int) getCoordinateValue}(0). If they are
     * different, the object with the higher coordinate is considered greater.</li>
     * </ul>
     * <p>
     * It is unlikely to be useful in reality to compare GridCoordinates objects
     * with different numbers of dimensions, but we specify this for
     * completeness.
     * </p>
     * <p>
     * This ordering ensures that collections of GridCoordinates objects will be
     * ordered with the last coordinate varying fastest.
     * </p>
     * 
     * @param c1
     *            The first set of coordinates to be compared
     * @param c2
     *            The second set of coordinates to be compared
     * @return a negative integer, zero, or a positive integer as {@code c1} is
     *         less than, equal to, or greater than {@code c2}.
     */
    @Override
    public int compare(GridCoordinates c1, GridCoordinates c2) {
        int diff = c1.getDimension() - c2.getDimension();
        if (diff != 0)
            return diff;
        // If we get this far the objects have the same number of dimensions
        for (int i = 0; i < c1.getDimension(); i++) {
            diff = c1.getCoordinateValue(i) - c2.getCoordinateValue(i);
            if (diff != 0)
                return diff;
        }
        return 0; // If we get this far the objects are equal
    }

}
