package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.Comparator;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;

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
 * @author Guy Griffiths
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
     * <li>If the two objects have a different number of dimensions, the one
     * with more dimensions is considered to be larger
     * <li>If the x-index of c1 is greater than the x-index of c2, then c1 is
     * considered to be larger than c1</li>
     * <li>If the x-indices of c1 and c2 are identical, comparison is done on
     * the y-index</li>
     * </ul>
     * <p>
     * This ordering ensures that collections of GridCoordinates objects will be
     * ordered with the y-coordinate varying fastest.
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
        if (c1.getNDim() != c2.getNDim()) {
            return new Integer(c1.getNDim()).compareTo(c2.getNDim());
        }
        for (int i = 0; i < c1.getNDim(); i++) {
            int diff = c1.getIndex(i) - c2.getIndex(i);
            if (diff != 0)
                return diff;
        }
        return 0; // If we get this far the objects are equal
    }

}
