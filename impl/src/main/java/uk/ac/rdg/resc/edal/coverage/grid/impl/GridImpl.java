/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.coverage.grid.Grid;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;

/**
 * Simple implementation of a {@link Grid} with no values or georeferencing
 * @author Jon
 */
public class GridImpl extends AbstractGrid {
    
    private final GridAxis xAxis;
    private final GridAxis yAxis;
    
    /**
     * Creates a Grid with axes of the given length (>= 1) and names "x" and "y"
     * respectively.
     * @throws IllegalArgumentException if the lengths are not both >= 1
     */
    public GridImpl(int xAxisSize, int yAxisSize)
    {
        this("x", xAxisSize, "y", yAxisSize);
    }
    
    public GridImpl(String xAxisName, int xAxisSize, String yAxisName, int yAxisSize)
    {
        if (xAxisSize < 1 || yAxisSize < 1) {
            throw new IllegalArgumentException("Axis lengths must be >= 0");
        }
        this.xAxis = new GridAxisImpl(xAxisName, xAxisSize);
        this.yAxis = new GridAxisImpl(yAxisName, yAxisSize);
    }
    
    public GridImpl(GridAxis xAxis, GridAxis yAxis)
    {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    @Override
    public GridAxis getXAxis() {
        return this.xAxis;
    }

    @Override
    public GridAxis getYAxis() {
        return this.yAxis;
    }
}
