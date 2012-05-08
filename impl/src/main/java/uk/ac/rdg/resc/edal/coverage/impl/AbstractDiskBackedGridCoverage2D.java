/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.List;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Skeletal implementation of GridCoverage2D, suitable for use when wrapping
 * disk-based storage, in which data-reading operations may be slow.
 * @author Jon
 */
public abstract class AbstractDiskBackedGridCoverage2D extends AbstractGridCoverage2D
{
    @Override
    void extractCoverageValues(String memberName, PixelMap pixelMap, List<Object> values)
    {
        GridDataSource<?> dataSource = this.openDataSource(memberName);
        
        // Select the data-reading strategy
        
        
        // Read the data according to the strategy
        
        // Close the data source
        dataSource.close();
        
    }

    /**
     * Returns an implementation of BigList that uses bulk reading methods
     * to implement getAll().
     */
    @Override
    public BigList<?> getValues(final String memberName)
    {
        return new AbstractMemberBigList<Object>(memberName)
        {
            @Override public Object get(long index)
            {
                GridCoordinates2D coords = getDomain().getCoords(index);
                GridDataSource<?> dataSource = openDataSource(memberName);
                Object value = dataSource.readPoint(coords.getXIndex(), coords.getYIndex());
                dataSource.close();
                return value;
            }

            @Override public List<Object> getAll(List<Long> indices)
            {
                GridDataSource<?> dataSource = openDataSource(memberName);
                List<Object> values = new ArrayList<Object>(indices.size());
                for (long index : indices)
                {
                    GridCoordinates2D coords = getDomain().getCoords(index);
                    Object value = dataSource.readPoint(coords.getXIndex(), coords.getYIndex());
                    values.add(value);
                }
                dataSource.close();
                return values;
            }
        };
    }
    
    protected abstract GridDataSource<?> openDataSource(String memberName);
    
}
