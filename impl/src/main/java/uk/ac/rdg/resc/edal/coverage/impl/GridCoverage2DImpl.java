package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.plugins.Plugin;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * An implementation of a {@link GridCoverage2D}. By basing this on
 * {@link AbstractMultimemberDiscreteGridCoverage}, we can add {@link Plugin}s
 * and add members arbitrarily by just supplying the correct
 * {@link GridValuesMatrix}
 * 
 * @author Guy Griffiths
 * 
 */
public class GridCoverage2DImpl extends
        AbstractMultimemberDiscreteGridCoverage<HorizontalPosition, GridCell2D, HorizontalGrid>
        implements GridCoverage2D {

    private final DataReadingStrategy strategy;

    public GridCoverage2DImpl(String description, HorizontalGrid domain,
            DataReadingStrategy strategy) {
        super(description, domain);
        this.strategy = strategy;
    }

    @Override
    public HorizontalGrid getDomain() {
        /*
         * This cast is fine, because the domain has been set from the
         * constructor (which constrains it to a HorizontalGrid)
         */
        return super.getDomain();
    }

    @Override
    public GridCoverage2D extractGridCoverage(HorizontalGrid targetGrid, Set<String> memberNames) {
        if (targetGrid.size() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Target grid too large");
        }

        Map<String, List<Object>> valuesMap = CollectionUtils.newLinkedHashMap();
        Map<String, ScalarMetadata> metadataMap = CollectionUtils.newLinkedHashMap();
        // Read the data from the source coverage
        for (String name : memberNames) {
            List<Object> values = strategy.readValues(this.getGridValues(name), this.getDomain(),
                    targetGrid);

            valuesMap.put(name, values);
            metadataMap.put(name, this.getRangeMetadata(name));
        }

        // Now assemble the remaining properties of the target coverage
        return new InMemoryGridCoverage2D(targetGrid, valuesMap, metadataMap,
                "Interpolated grid from " + getDescription());
    }
}