/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.Set;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.Record;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.AbstractGridValuesMatrix;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Skeletal implementation of GridCoverage2D, providing a default implementation
 * of {@link #getGridValues(java.lang.String)}.  This class is most suitable
 * for memory-resident data
 * @author Jon
 */
public abstract class AbstractGridCoverage2D extends AbstractDiscreteCoverage<HorizontalPosition, GridCell2D> implements GridCoverage2D
{

    @Override
    public GridValuesMatrix<?> getGridValues(final String memberName) {
        return new AbstractGridValuesMatrix() {

            @Override
            public GridAxis getXAxis() {
                return getDomain().getXAxis();
            }

            @Override
            public GridAxis getYAxis() {
                return getDomain().getYAxis();
            }

            @Override
            public Object getValue(int i, int j) {
                return AbstractGridCoverage2D.this.getValue(memberName, i, j);
            }
            
            @Override
            public BigList<?> getValues() {
                return AbstractGridCoverage2D.this.getValues(memberName);
            }

            @Override
            public Class<?> getValueType() {
                return getRangeType().getValueType(memberName);
            }
            
        };
    }
    
    /**
     * <p>Gets the value of the given coverage member at the given grid coordinates.
     * All other data-reading operations are based on this, although subclasses
     * can of course override this behaviour.  Therefore implementations of this
     * method should be "fast" (i.e. should avoid reading from slow storage)
     * where possible.</p>
     * @throws IndexOutOfBoundsException if i or j are out of bounds
     * @throws IllegalArgumentException if memberName is not in the set of member
     * names for this coverage.
     */
    protected abstract Object getValue(String memberName, int i, int j);
    
    @Override
    public BigList<?> getValues(final String memberName) {
        return new AbstractBigList() {

            @Override
            public Object get(long index) {
                GridCoordinates2D coords = getDomain().getCoords(index);
                return getValue(memberName, coords.getXIndex(), coords.getYIndex());
            }

            @Override
            public long sizeAsLong() {
                return getDomain().size();
            }

            @Override
            public Class<?> getValueType() {
                return AbstractGridCoverage2D.this.getValueType(memberName);
            }
            
        };
    }

    @Override
    public GridCoverage2D extractGridCoverage(HorizontalGrid targetGrid, Set<String> memberNames) {
        for (GridCell2D targetCell : targetGrid.getDomainObjects()) {
            HorizontalPosition centre = targetCell.getCentre();
            Record rec = this.evaluate(centre, memberNames);
            
        }
        
        throw new UnsupportedOperationException("TODO: finish this method!");
    }
    
}

class test extends AbstractGridCoverage2D
{

    @Override
    protected Object getValue(String memberName, int i, int j) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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