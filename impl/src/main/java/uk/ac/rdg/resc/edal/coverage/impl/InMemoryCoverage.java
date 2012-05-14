package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.InMemoryGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

public class InMemoryCoverage extends AbstractGridCoverage2D {

    private HorizontalGrid domain;
    private Map<String, List<?>> values;
    private String description;
    private int xSize;
    

    public InMemoryCoverage(HorizontalGrid domain, Map<String, List<?>> values, String description) {
        this.domain = domain;
        this.values = values;
        this.description = description;
        xSize = domain.getXAxis().size();
    }
    
    @Override
    public HorizontalGrid getDomain() {
        return domain;
    }

    @Override
    public GridValuesMatrix<?> getGridValues(final String memberName) {
        this.checkMemberName(memberName);
        
        return new InMemoryGridValuesMatrix<Object>() {
            @Override
            public Object readPoint(int i, int j) {
                int index = i + j * xSize;
                return values.get(memberName).get(index);
            }

            @Override
            public Class<Object> getValueType() {
                /*
                 * TODO this is not right, but how do we get the value type?
                 */
                return (Class<Object>)(Class)Float.class;
            }

            @Override
            public GridAxis getXAxis() {
                return domain.getXAxis();
            }

            @Override
            public GridAxis getYAxis() {
                return domain.getYAxis();
            }
        };
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getMemberNames() {
        return values.keySet();
    }

    @Override
    public ScalarMetadata getRangeMetadata(String memberName) {
        this.checkMemberName(memberName);
        throw new UnsupportedOperationException("This is not yet implemented");
//        return null;
    }

    @Override
    protected DataReadingStrategy getDataReadingStrategy() {
        // This is an in-memory GVM and so this strategy is most efficient
        return DataReadingStrategy.PIXEL_BY_PIXEL;
    }

}
