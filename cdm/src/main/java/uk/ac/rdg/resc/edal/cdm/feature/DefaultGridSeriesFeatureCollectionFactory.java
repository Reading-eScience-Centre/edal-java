package uk.ac.rdg.resc.edal.cdm.feature;

import java.io.IOException;

import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;

public class DefaultGridSeriesFeatureCollectionFactory extends GridSeriesFeatureCollectionFactory {

    @Override
    public FeatureCollection<GridSeriesFeature<?>> read(String location, String id, String name) throws IOException {
        return new NcGridSeriesFeatureCollection(id, name, location);
    }
}
