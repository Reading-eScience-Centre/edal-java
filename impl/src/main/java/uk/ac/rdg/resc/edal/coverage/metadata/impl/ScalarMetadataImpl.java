package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import java.util.Collections;
import java.util.Set;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

public class ScalarMetadataImpl implements ScalarMetadata {

    private final String name;
    private final String description;
    private final Phenomenon parameter;
    private final Unit units;

    public ScalarMetadataImpl(String name, String description, Phenomenon parameter, Unit units) {
        this.name = name;
        this.description = description;
        this.parameter = parameter;
        this.units = units;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Phenomenon getParameter() {
        return parameter;
    }

    @Override
    public Unit getUnits() {
        return units;
    }

    @Override
    public RangeMetadata getMemberMetadata(String memberName) {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getMemberNames() {
        return Collections.emptySet();
    }

    @Override
    public RangeMetadata getParent() { return null; }
    
    
    // TODO equals() and hashCode()
}
