package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.coverage.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;

/**
 * Immutable implementation of a {@link RectilinearGrid} using {@link Double}s.
 * 
 * @author Guy Griffiths
 */
public final class RectilinearGridImpl extends AbstractRectilinearGrid {
    private final ReferenceableAxis<Double> xAxis;
    private final ReferenceableAxis<Double> yAxis;

    public RectilinearGridImpl(ReferenceableAxis<Double> xAxis, ReferenceableAxis<Double> yAxis,
                               CoordinateReferenceSystem crs) {
        super(crs);
        if (xAxis == null || yAxis == null) {
            throw new NullPointerException("Axes cannot be null");
        }
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    public RectilinearGridImpl(ReferenceableAxis<Double> xAxis, ReferenceableAxis<Double> yAxis) {
        this(xAxis, yAxis, null);
    }

    @Override
    public ReferenceableAxis<Double> getXAxis() {
        return xAxis;
    }

    @Override
    public ReferenceableAxis<Double> getYAxis() {
        return yAxis;
    }
}
