package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.RecordType;
import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.InMemoryGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

public class InMemoryCoverage extends AbstractGridCoverage2D {

    private HorizontalGrid domain;
    private Map<String, List<?>> values;
    private String description;
    private RangeMetadata rangeMetadata;
    private int xSize;

    public InMemoryCoverage(HorizontalGrid domain, Map<String, List<?>> values, RangeMetadata recordType, String description) {
        this.domain = domain;
        this.values = values;
        this.rangeMetadata = recordType;
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

            @SuppressWarnings("unchecked")
            @Override
            public Class<Object> getValueType() {
                RangeMetadata memberMetadata = rangeMetadata.getMemberMetadata(memberName);
                if(memberMetadata instanceof ScalarMetadata){
                    ScalarMetadata scalarMetadata = (ScalarMetadata) memberMetadata;
                    return (Class<Object>) scalarMetadata.getValueType();
                } else {
                    return (Class<Object>)(Class)RangeMetadata.class;
                }
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
        return (ScalarMetadata) rangeMetadata.getMemberMetadata(memberName);
    }

    @Override
    protected DataReadingStrategy getDataReadingStrategy() {
        // This is an in-memory GVM and so this strategy is most efficient
        return DataReadingStrategy.PIXEL_BY_PIXEL;
    }

}
