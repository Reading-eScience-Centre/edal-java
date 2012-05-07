/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.Set;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Skeletal implementation of GridCoverage2D, suitable for use when wrapping
 * disk-based storage, in which data-reading operations may be slow.
 * @author Jon
 */
public abstract class AbstractDiskBackedGridCoverage2D extends
        AbstractDiscreteCoverage<HorizontalPosition, GridCell2D> implements GridCoverage2D
{

    @Override
    public BigList<?> getValues(String memberName) {
        return new AbstractBigList() {

            @Override
            public Object get(long index) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Class getValueType() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public long sizeAsLong() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        
        };
    }

    @Override
    public GridValuesMatrix<?> getGridValues(String memberName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GridCoverage2D extractGridCoverage(HorizontalGrid targetGrid, Set<String> memberNames) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

class test2 extends AbstractDiskBackedGridCoverage2D
{

    @Override
    protected Class<?> getValueType(String memberName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String getDescription(String memberName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Unit getUnits(String memberName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Phenomenon getParameter(String memberName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> getMemberNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HorizontalGrid getDomain() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}