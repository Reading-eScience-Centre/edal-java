/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 * An in-memory GridCoverage2D
 * @author Jon
 */
public class GridCoverage2DImpl extends AbstractGridCoverage2D 
{
    
    private final String description;
    private final HorizontalGrid domain;
    private final Map<String, ScalarMetadata> metadata;
    private final Map<String, List<?>> values;
    
    public GridCoverage2DImpl(String description, HorizontalGrid domain,
            Map<String, ScalarMetadata> metadata, Map<String, List<?>> values)
    {
        this.description = description;
        this.domain = domain;
        this.metadata = metadata;
        this.values = values;
    }

    @Override
    protected DataReadingStrategy getDataReadingStrategy() {
        // This is an in-memory structure so this strategy is appropriate
        return DataReadingStrategy.PIXEL_BY_PIXEL;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Set<String> getMemberNames() {
        return this.values.keySet();
    }

    @Override
    public ScalarMetadata getRangeMetadata(String memberName) {
        this.checkMemberName(memberName);
        return this.metadata.get(memberName);
    }

    @Override
    public HorizontalGrid getDomain() { return this.domain; }

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
    
    
}
