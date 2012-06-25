package uk.ac.rdg.resc.edal.feature.impl;

import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;

/**
 * A partial implementation of a {@link Feature}
 * 
 * @author Jon Blower
 * @author Guy Griffiths
 * 
 */
public abstract class AbstractFeature implements Feature {
    
    private String name;
    private String description;
    private String id;
    private final FeatureCollection<? extends Feature> parentCollection;

    public AbstractFeature(String name, String id, String description,
            FeatureCollection<? extends Feature> parentCollection) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.parentCollection = parentCollection;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FeatureCollection<? extends Feature> getFeatureCollection() {
        return parentCollection;
    }
}
