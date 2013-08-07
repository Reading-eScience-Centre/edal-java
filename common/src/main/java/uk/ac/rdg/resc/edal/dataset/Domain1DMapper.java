package uk.ac.rdg.resc.edal.dataset;

import java.util.Collection;

import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * This is an implementation of a {@link DomainMapper} which maps 2D indices
 * from a source grid onto a single integer in the target domain.
 * 
 * It also includes a static method
 * {@link Domain1DMapper#forList(HorizontalGrid, Collection)} which generates a
 * {@link Domain1DMapper} from a source grid and a list of target positions.
 * 
 * @author Guy
 */
public class Domain1DMapper extends DomainMapper<Integer> {
    protected Domain1DMapper(HorizontalGrid sourceGrid, long targetDomainSize) {
        super(sourceGrid, targetDomainSize);
    }

    @Override
    protected Integer convertIndexToCoordType(int index) {
        /*
         * We're mapping from an int to an int, so we just return it.
         */
        return index;
    }

    /**
     * Creates a {@link Domain1DMapper} from a {@link HorizontalGrid} source and
     * a {@link Collection} of {@link HorizontalPosition}s for the target
     * 
     * @param sourceGrid
     *            A {@link HorizontalGrid} on which the source data is defined
     * @param targetPositions
     *            A {@link Collection} of {@link HorizontalPosition}s defining
     *            the target domain.
     * @return A {@link Domain1DMapper} which performs the mapping
     */
    public static Domain1DMapper forList(HorizontalGrid sourceGrid,
            Collection<HorizontalPosition> targetPositions) {
        log.debug("Creating DomainMapper from a 1D list of points");

        long start = System.currentTimeMillis();
        Domain1DMapper mapper = new Domain1DMapper(sourceGrid, targetPositions.size());
        int pixelIndex = 0;
        /*
         * Find the nearest grid coordinates to all the points in the domain
         */
        for (HorizontalPosition pos : targetPositions) {
            /*
             * Find the index of the cell containing this position
             */
            int[] indices = sourceGrid.findIndexOf(pos);
            mapper.put(indices[0], indices[1], pixelIndex);
            pixelIndex++;
        }
        long end = System.currentTimeMillis();
        mapper.sortIndices();
        log.debug("DomainMapper created in " + (end - start) + "ms");
        return mapper;
    }
}
