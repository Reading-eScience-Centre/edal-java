/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * Simple immutable implementation of a {@link GridAxis}
 * @author Jon
 */
public class GridAxisImpl implements GridAxis
{
    private final String name;
    private final int size;
    
    public GridAxisImpl(String name, int size)
    {
        this.name = name;
        this.size = size;
    }

    @Override
    public String getName() { return this.name; }

    /**
     * {@inheritDoc}
     * <p>This implementation returns an Extent from 0 to size -1.</p>
     * @return 
     */
    @Override
    public Extent<Integer> getIndexExtent() {
        return Extents.newExtent(0, this.size - 1);
    }

    @Override
    public int size() { return this.size; }
    
}
