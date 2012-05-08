/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.List;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.impl.PixelMap.PixelMapEntry;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Skeletal implementation of GridCoverage2D, suitable for use when wrapping
 * in-memory storage, in which data reading is assumed to be fast.
 * @author Jon
 */
public abstract class AbstractInMemoryGridCoverage2D extends AbstractGridCoverage2D
{
    protected abstract Object getValue(String memberName, int i, int j);

    @Override
    public BigList<?> getValues(final String memberName)
    {
        return new AbstractMemberBigList(memberName)
        {
            @Override public Object get(long index)
            {
                GridCoordinates2D coords = getDomain().getCoords(index);
                return getValue(memberName, coords.getXIndex(), coords.getYIndex());
            }
        };
    }
    
    @Override
    void extractCoverageValues(String memberName, PixelMap pixelMap, List<Object> values)
    {
        for (PixelMapEntry entry : pixelMap)
        {
            Object value = this.getValue(memberName, entry.getSourceGridIIndex(), entry.getSourceGridJIndex());
            for (int index : entry.getTargetGridPoints())
            {
                values.set(index, value);
            }
        }
    }
}

