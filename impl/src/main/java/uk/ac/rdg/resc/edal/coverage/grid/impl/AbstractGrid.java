package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.Grid;

/**
 * Abstract superclass that implements the {@link #getSize()} and
 * {@link #getDimension()} methods of a Grid
 * based upon the GridEnvelope that is supplied by subclasses
 * @author Jon
 */
public abstract class AbstractGrid implements Grid
{
    /**
     * {@inheritDoc}
     * <p>This implementation uses the {@link #getGridExtent() GridEnvelope}
     * provided by subclasses.</p>
     */
    @Override
    public final int size() {
        // We reuse code in GridExtentImpl to calculate the size
        return GridExtentImpl.convert(getGridExtent()).getSize();
    }

    /**
     * {@inheritDoc}
     * <p>This implementation uses the {@link #getGridExtent() GridEnvelope}
     * provided by subclasses.</p>
     */
    @Override
    public final int getDimension() {
        return getGridExtent().getDimension();
    }

}
