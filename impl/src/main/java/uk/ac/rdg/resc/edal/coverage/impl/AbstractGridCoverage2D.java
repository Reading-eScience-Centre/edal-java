/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.impl.PixelMap.PixelMapEntry;
import uk.ac.rdg.resc.edal.coverage.impl.PixelMap.Scanline;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.BigList;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * Skeletal implementation of GridCoverage2D.
 * @author Jon
 */
public abstract class AbstractGridCoverage2D extends AbstractDiscreteCoverage<HorizontalPosition, GridCell2D> implements GridCoverage2D
{

    @Override
    public GridCoverage2D extractGridCoverage(HorizontalGrid targetGrid, Set<String> memberNames)
    {
        if (targetGrid.size() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Target grid too large");
        }
        int targetGridSize = (int)targetGrid.size();
        
        // First we calculate the mapping between this Coverage's grid and
        // the target grid
        PixelMap pixelMap = PixelMap.forGrid(this.getDomain(), targetGrid);
        
        Map<String, List<?>> valuesMap = CollectionUtils.newLinkedHashMap();
        
        // Read the data from the source coverage
        for (String name : memberNames)
        {
            List<Object> values = listOfNulls(targetGridSize);
            this.extractCoverageValues(name, pixelMap, values);
            valuesMap.put(name, values);
        }
        
        // Now assemble the remaining properties of the target coverage
        
        return new InMemoryCoverage(targetGrid, valuesMap, "Interpolated grid from "+getDescription());
    }
    
    /**
     * Returns the strategy that should be used to read data from this coverage
     */
    protected abstract DataReadingStrategy getDataReadingStrategy();
    
    private void extractCoverageValues(String memberName, PixelMap pixelMap, List<Object> values)
    {
        GridValuesMatrix<?> gridValues = this.getGridValues(memberName);
        // TODO: for in-memory GridValuesMatrixes, the pixel-by-pixel strategy
        // will usually be the most efficient
        DataReadingStrategy strategy = this.getDataReadingStrategy();
        
        if (strategy == DataReadingStrategy.BOUNDING_BOX)
        {
            readDataBoundingBox(gridValues, pixelMap, values);
        }
        else if (strategy == DataReadingStrategy.SCANLINE)
        {
            readDataScanline(gridValues, pixelMap, values);
        }
        else if (strategy == DataReadingStrategy.PIXEL_BY_PIXEL)
        {
            readDataPixelByPixel(gridValues, pixelMap, values);
        }
        else
        {
            throw new IllegalStateException("Unknown strategy");
        }
        
        // Close the data source
        gridValues.close();
    }
    
    private static void readDataBoundingBox(GridValuesMatrix<?> gridValues,
            PixelMap pixelMap, List<Object> values)
    {
        int imin = pixelMap.getMinIIndex();
        int imax = pixelMap.getMaxIIndex();
        int jmin = pixelMap.getMinJIndex();
        int jmax = pixelMap.getMaxJIndex();
        
        GridValuesMatrix<?> block = gridValues.readBlock(imin, imax, jmin, jmax);

        for (PixelMapEntry pme : pixelMap) {
            int i = pme.getSourceGridIIndex() - imin;
            int j = pme.getSourceGridJIndex() - jmin;
            Object val = block.readPoint(i, j);
            for (int targetGridPoint : pme.getTargetGridPoints()) {
                values.set(targetGridPoint, val);
            }
        }
        // This will probably do nothing, because the result of readBlock() will
        // be an in-memory structure.
        block.close();
    }
    
    private static void readDataScanline(GridValuesMatrix<?> gridValues,
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
            
            GridValuesMatrix<?> block = gridValues.readBlock(imin, imax, j, j);
            
            for (PixelMapEntry pme : entries)
            {
                int i = pme.getSourceGridIIndex() - imin;
                Object val = block.readPoint(i, 0);
                for (int p : pme.getTargetGridPoints())
                {
                    values.set(p, val);
                }
            }
            // This will probably do nothing, because the result of readBlock() will
            // be an in-memory structure.
            block.close();
        }
    }
    
    private void readDataPixelByPixel(GridValuesMatrix<?> gridValues,
            PixelMap pixelMap, List<Object> values)
    {
        for (PixelMapEntry pme : pixelMap)
        {
            Object value = gridValues.readPoint(pme.getSourceGridIIndex(), pme.getSourceGridJIndex());
            for (int index : pme.getTargetGridPoints())
            {
                values.set(index, value);
            }
        }
    }
    
    /**
     * Creates and returns a new mutable list consisting entirely of null values.
     * The values of the list can be altered through set(), but the size of the list
     * cannot be altered.
     * @param size The size of the list to create
     * @return a new mutable list consisting entirely of null values.
     * @todo intelligently create lists of different types depending on the value
     * type.
     */
    private static List<Object> listOfNulls(int size)
    {
        final Object[] arr = new Object[size];
        Arrays.fill(arr, null);
        return new AbstractList<Object>() {

            @Override
            public Object get(int index) { return arr[index]; }
            
            @Override 
            public Object set(int index, Object newValue) {
                Object oldValue = arr[index];
                arr[index] = newValue;
                return oldValue;
            }

            @Override
            public int size() { return arr.length; }
            
        };
    } 
    
    /**
     * Returns an implementation of BigList that uses bulk reading methods
     * to implement getAll().  Generally speaking clients should use:
     * <pre>
     * GridValueaMatrix<?> gridValues = getGridValues(memberName);
     * BigList<?> bigList = gridValues.getValues();
     * ... // do something with the BigList
     * gridValues.close();
     * </pre>
     * instead, for efficiency reasons.
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
                GridValuesMatrix<?> gridValues = getGridValues(memberName);
                Object value = gridValues.readPoint(coords.getXIndex(), coords.getYIndex());
                gridValues.close();
                return value;
            }

            @Override public List<Object> getAll(List<Long> indices)
            {
                GridValuesMatrix<?> gridValues = getGridValues(memberName);
                List<Object> values = new ArrayList<Object>(indices.size());
                for (long index : indices)
                {
                    GridCoordinates2D coords = getDomain().getCoords(index);
                    Object value = gridValues.readPoint(coords.getXIndex(), coords.getYIndex());
                    values.add(value);
                }
                gridValues.close();
                return values;
            }
        };
    }
    
}
