package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.InMemoryGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.util.BigList;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * An in-memory implementation of {@link GridCoverage2D}
 * 
 * @author Guy Griffiths
 * 
 */
public class InMemoryGridCoverage2D extends GridCoverage2DImpl {

    private final Map<String, ScalarMetadata> metadata;
    private final Map<String, List<Object>> values;

    /**
     * Instantiates a new {@link InMemoryGridCoverage2D}
     * 
     * @param domain
     *            the domain of the coverage
     * @param values
     *            a {@link Map} of member names to {@link List}s of values
     * @param metadata
     *            a {@link Map} of member names to {@link ScalarMetadata}
     * @param description
     *            a description of the coverage
     */
    public InMemoryGridCoverage2D(HorizontalGrid domain, Map<String, List<Object>> values,
            Map<String, ScalarMetadata> metadata, String description) {
        super(description, domain, DataReadingStrategy.PIXEL_BY_PIXEL);
        if (values.size() != metadata.size() || !values.keySet().equals(metadata.keySet())) {
            throw new IllegalArgumentException(
                    "Both values and metadata must contain the same members");
        }
        this.values = values;
        this.metadata = metadata;
    }

    @Override
    public GridValuesMatrix<Object> getGridValues(final String memberName) {
        this.checkMemberName(memberName);

        return new InMemoryGridValuesMatrix<Object>() {
            @Override
            public Object doReadPoint(int[] coords) {
                int index = (int) getDomain().getIndex(coords[0], coords[1]);
                List<Object> vals = values.get(memberName);
                return vals.get(index);
            }

            @Override
            public BigList<Object> getValues() {
                return CollectionUtils.wrap(values.get(memberName));
            }

            @Override
            public GridAxis doGetAxis(int n) {
                switch (n) {
                case 0:
                    return getDomain().getXAxis();
                case 1:
                    return getDomain().getYAxis();
                default:
                    /*
                     * We should never reach this code, because getAxis will
                     * already have checked the bounds
                     */
                    throw new IllegalStateException("Axis index out of bounds");
                }
            }

            @Override
            public int getNDim() {
                return 2;
            }

            @Override
            @SuppressWarnings({ "rawtypes", "unchecked" })
            public Class<Object> getValueType() {
                return (Class) metadata.get(memberName).getValueType();
            }
        };
    }

    @Override
    public Set<String> getMemberNames() {
        return values.keySet();
    }

    @Override
    public ScalarMetadata getRangeMetadata(String memberName) {
        this.checkMemberName(memberName);
        return this.metadata.get(memberName);
    }
}
