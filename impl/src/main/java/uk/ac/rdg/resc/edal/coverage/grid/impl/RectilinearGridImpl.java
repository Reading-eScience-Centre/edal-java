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

    /**
     * Instantiates a new rectilinear grid from the given axes
     * 
     * @param xAxis
     *            the x-axis
     * @param yAxis
     *            the y-axis
     * @param crs
     *            the {@link CoordinateReferenceSystem}
     */
    public RectilinearGridImpl(ReferenceableAxis<Double> xAxis, ReferenceableAxis<Double> yAxis,
            CoordinateReferenceSystem crs) {
        super(crs);
        if (xAxis == null || yAxis == null) {
            throw new NullPointerException("Axes cannot be null");
        }
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    /**
     * Instantiates a new rectilinear grid from the given axes, with no
     * {@link CoordinateReferenceSystem}
     * 
     * @param xAxis
     *            the x-axis
     * @param yAxis
     *            the y-axis
     */
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RectilinearGridImpl) {
            RectilinearGridImpl grid = (RectilinearGridImpl) obj;
            return grid.xAxis.equals(xAxis) && grid.yAxis.equals(yAxis) && super.equals(obj);
        } else {
            return false;
        }
    }
}
