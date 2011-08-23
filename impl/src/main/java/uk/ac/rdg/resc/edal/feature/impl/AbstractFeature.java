package uk.ac.rdg.resc.edal.feature.impl;

import uk.ac.rdg.resc.edal.feature.Feature;

public abstract class AbstractFeature implements Feature {
    
    private String name;
    private String description;
    private String id;

    public AbstractFeature(String name, String id, String description) {
        this.name = name;
        this.description = description;
        this.id = id;
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
}
