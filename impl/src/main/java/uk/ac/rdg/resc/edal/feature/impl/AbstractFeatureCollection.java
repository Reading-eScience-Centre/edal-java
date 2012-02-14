package uk.ac.rdg.resc.edal.feature.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;

public abstract class AbstractFeatureCollection<R extends Feature> implements FeatureCollection<R> {
    private String collectionId;
    private String name;
    protected Map<String, R> id2Feature;

    public AbstractFeatureCollection(String collectionId, String collectionName)
            throws IOException {
        this.collectionId = collectionId;
        this.name = collectionName;

        id2Feature = new HashMap<String, R>();
    }

    @Override
    public R getFeatureById(String id) {
        return id2Feature.get(id);
    }

    @Override
    public Set<String> getFeatureIds() {
        return id2Feature.keySet();
    }

    @Override
    public Collection<R> getFeatures() {
        return id2Feature.values();
    }

    @Override
    public String getId() {
        return collectionId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Iterator<R> iterator() {
        return id2Feature.values().iterator();
    }
}
