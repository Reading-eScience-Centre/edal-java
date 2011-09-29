package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridExtent;

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
    public final long size() {
        // We reuse code in GridExtentImpl to calculate the size
        return GridExtentImpl.convert(getGridExtent()).size();
    }

    @Override
    public GridExtent getGridExtent() {
        return new GridExtentImpl(getXAxis().getIndexExtent(), getYAxis().getIndexExtent());
    }

}
