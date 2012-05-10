/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.impl.PixelMap.PixelMapEntry;
import uk.ac.rdg.resc.edal.coverage.impl.PixelMap.Scanline;
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
        DataReadingStrategy strategy = this.getDataReadingStrategy();
        
        if (strategy == DataReadingStrategy.BOUNDING_BOX)
        {
            readDataBoundingBox(dataSource, pixelMap, values);
        }
        else if (strategy == DataReadingStrategy.SCANLINE)
        {
            readDataScanline(dataSource, pixelMap, values);
        }
        else if (strategy == DataReadingStrategy.PIXEL_BY_PIXEL)
        {
            throw new UnsupportedOperationException("Not supported yet");
        }
        else
        {
            throw new IllegalStateException("Unknown strategy");
        }
        
        // Close the data source
        dataSource.close();
    }
    
    private static void readDataBoundingBox(GridDataSource<?> dataSource,
            PixelMap pixelMap, List<Object> values)
    {
        int imin = pixelMap.getMinIIndex();
        int imax = pixelMap.getMaxIIndex();
        int jmin = pixelMap.getMinJIndex();
        int jmax = pixelMap.getMaxJIndex();
        int iSize = imax - imin + 1;
        
        List<?> block = dataSource.readBlock(imin, imax, jmin, jmax);

        for (PixelMapEntry pme : pixelMap) {
            int i = pme.getSourceGridIIndex() - imin;
            int j = pme.getSourceGridJIndex() - jmin;
            int indexInList = j * iSize + i;
            Object val = block.get(indexInList);
            for (int targetGridPoint : pme.getTargetGridPoints()) {
                values.set(targetGridPoint, val);
            }
        }
    }
    
    private static void readDataScanline(GridDataSource<?> dataSource,
            PixelMap pixelMap, List<Object> values)
    {
        Iterator<Scanline> it = pixelMap.scanlineIterator();
        while (it.hasNext())
        {
            Scanline scanline = it.next();
            List<PixelMapEntry> entries = scanline.getPixelMapEntries();
            int entriesSize = entries.size();
            
            int j = scanline.getSourceGridJIndex();
            int imin = entries.get(0).getSourceGridIIndex();
            int imax = entries.get(entriesSize - 1).getSourceGridIIndex();
            
            List<?> data = dataSource.readScanline(j, imin, imax);
            
            for (PixelMapEntry pme : entries)
            {
                int i = pme.getSourceGridIIndex() - imin;
                Object val = data.get(i);
                for (int p : pme.getTargetGridPoints())
                {
                    values.set(p, val);
                }
            }
        }
    }

    /**
     * Returns an implementation of BigList that uses bulk reading methods
     * to implement getAll().
     * @todo make getAll(from, to) more efficient
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
    
    /**
     * Returns the strategy that should be used to read data from this coverage
     */
    protected abstract DataReadingStrategy getDataReadingStrategy();
    
}
