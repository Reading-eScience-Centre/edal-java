package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.InMemoryGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.util.BigList;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

public class InMemoryCoverage extends AbstractGridCoverage2D {

    private final HorizontalGrid domain;
    private final Map<String, ScalarMetadata> metadata;
    private final Map<String, List<?>> values;
    private final String description;
    

    public InMemoryCoverage(HorizontalGrid domain, Map<String, List<?>> values, 
            Map<String, ScalarMetadata> metadata, String description) {
        this.domain = domain;
        this.values = values;
        this.metadata = metadata;
        this.description = description;
    }
    
    @Override
    public HorizontalGrid getDomain() {
        return domain;
    }

    @Override
    public GridValuesMatrix<?> getGridValues(final String memberName)
    {
        this.checkMemberName(memberName);
        
        return new InMemoryGridValuesMatrix<Object>()
        {
            @Override
            public Object readPoint(int i, int j) {
                int index = (int)domain.getIndex(i, j);
                List<?> vals = values.get(memberName);
                return vals.get(index);
            }
            
            @Override
            public BigList<Object> getValues() {
                return (BigList<Object>)CollectionUtils.wrap(values.get(memberName));
            }
            
            @Override
            public GridAxis getXAxis() {
                return domain.getXAxis();
            }

            @Override
            public GridAxis getYAxis() {
                return domain.getYAxis();
            }

            @Override
            public Class<Object> getValueType() {
                return (Class<Object>)(Class)metadata.get(memberName).getValueType();
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
        return this.metadata.get(memberName);
    }

    @Override
    protected DataReadingStrategy getDataReadingStrategy() {
        // This is an in-memory GVM and so this strategy is most efficient
        return DataReadingStrategy.PIXEL_BY_PIXEL;
    }

}
