/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridValuesMatrixImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * Skeletal implementation of GridCoverage2D.
 * @author Jon
 */
public abstract class AbstractGridCoverage2D extends AbstractDiscreteCoverage<HorizontalPosition, GridCell2D> implements GridCoverage2D
{

    @Override
    public GridValuesMatrix<?> getGridValues(String memberName) {
        return new GridValuesMatrixImpl(this.getDomain(), this.getValues(memberName));
    }

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
        
        // Read the data from the source coverage
        for (String name : memberNames)
        {
            List<Object> values = listOfNulls(targetGridSize);
            this.extractCoverageValues(name, pixelMap, values);
        }
        
        // Now assemble the remaining properties of the target coverage
        
        
        return null;
    }
    
    abstract void extractCoverageValues(String memberName, PixelMap pixelMap, List<Object> values);
    
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
    
}
