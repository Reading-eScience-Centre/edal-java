/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.List;
import java.util.Set;
import javax.measure.unit.Unit;
import org.opengis.geometry.DirectPosition;
import uk.ac.rdg.resc.edal.coverage.DiscreteGridPointCoverage;
import uk.ac.rdg.resc.edal.coverage.grid.GridPoint;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractDiscreteCoverage;

/**
 *
 * @author Jon
 */
public class DiscreteGridPointCoverageImpl
        extends AbstractDiscreteCoverage<DirectPosition, GridPoint>
        implements DiscreteGridPointCoverage
{

    @Override
    protected int findDomainObject(DirectPosition position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Set<String> getFieldNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Class<?> getClass(String fieldName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Unit getUnitsOfMeasure(String fieldName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getDomainExtent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<GridPoint> getDomain() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<?> getRange(String fieldName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
