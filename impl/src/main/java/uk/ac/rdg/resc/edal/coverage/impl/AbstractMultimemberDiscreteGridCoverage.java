package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.BaseGridCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridDomain;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.plugins.Plugin;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * A partial implementation of a multimember coverage on a {@link GridDomain}.
 * This provides a method to add members by supplying a {@link GridValuesMatrix}
 * and some basic metadata
 * 
 * @author Guy Griffiths
 * 
 * 
 * @param <P>
 *            The type of object used to identify positions within the
 *            coverage's domain. This may be a spatial, temporal, or combined
 *            spatiotemporal position.
 * @param <DO>
 *            The type of domain object
 * @param <GD>
 *            The type of domain which members must be on
 */
public abstract class AbstractMultimemberDiscreteGridCoverage<P, DO, GD extends GridDomain<P, DO>>
        extends AbstractMultimemberDiscreteCoverage<P, DO, GD> implements BaseGridCoverage<P, DO> {

    private final Map<String, GridValuesMatrix<?>> gridValuesMatrices;

    public AbstractMultimemberDiscreteGridCoverage(String description, GD domain) {
        super(description, domain);
        gridValuesMatrices = new HashMap<String, GridValuesMatrix<?>>();
    }

    @Override
    public GridValuesMatrix<?> getGridValues(final String memberName) {
        if (!gridValuesMatrices.containsKey(memberName)) {
            throw new IllegalArgumentException(memberName + " is not present in this coverage");
        }
        if (plugins.containsKey(memberName)) {
            final Plugin plugin = plugins.get(memberName);
            final List<GridValuesMatrix<?>> pluginInputs = new ArrayList<GridValuesMatrix<?>>();
            for (String neededId : plugin.uses()) {
                pluginInputs.add(gridValuesMatrices.get(neededId));
            }
            return plugin.getProcessedValues(memberName, pluginInputs);
        } else {
            return gridValuesMatrices.get(memberName);
        }
    }

    public void addMember(String memberName, GD domain, String description, Phenomenon parameter,
            Unit units, GridValuesMatrix<?> gridValueMatrix) {
        addMemberToMetadata(memberName, domain, description, parameter, units);
        gridValuesMatrices.put(memberName, gridValueMatrix);
    }

    @Override
    public final BigList<?> getValuesList(final String memberName) {
        return new AbstractBigList2<Object>() {
            @Override
            public Object get(long index) {
                GridValuesMatrix<?> gridValues = getGridValues(memberName);
                GridCoordinates coords = gridValues.getCoords(index);
                Object value = gridValues.readPoint(coords.getIndices());
                return value;
            }

            @Override
            public List<Object> getAll(List<Long> indices) {
                GridValuesMatrix<?> gridValues = getGridValues(memberName);
                List<Object> values = new ArrayList<Object>(indices.size());
                for (long index : indices) {
                    GridCoordinates coords = gridValues.getCoords(index);
                    Object value = gridValues.readPoint(coords.getIndices());
                    values.add(value);
                }
                return values;
            }
        };
    }
}
