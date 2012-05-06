/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.AbstractGridValuesMatrix;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.AbstractBigList;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * Skeletal implementation of GridCoverage2D, providing a default implementation
 * of {@link #getGridValues(java.lang.String)}.
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
                return getRecordType().getValueType(memberName);
            }
            
        };
    }
    
    /**
     * Gets the value of the given coverage member at the given grid coordinates.
     * All other data-reading operations are based on this, although subclasses
     * can of course override this behaviour.
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
                return getRecordType().getValueType(memberName);
            }
            
        };
    }
    
}