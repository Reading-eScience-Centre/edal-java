/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.List;

/**
 * A source of data for {@link AbstractDiskBackedGridCoverage2D}s.
 * @author Jon
 */
public abstract class GridDataSource<E>
{
    public E readPoint(int i, int j)
    {
        return readBlock(i,i,j,j).get(0);
    }
    
    public List<E> readScanline(int j, int imin, int imax)
    {
        return readBlock(imin, imax, j, j);
    }
    
    public abstract List<E> readBlock(int imin, int imax, int jmin, int jmax);
    
    public abstract boolean isCompressed();
    
    public abstract boolean isRemote();
    
    public abstract void close();
    
}
