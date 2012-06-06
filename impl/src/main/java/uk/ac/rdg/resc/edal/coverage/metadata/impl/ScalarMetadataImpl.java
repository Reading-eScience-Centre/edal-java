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
    private final Class<?> clazz;
    private final RangeMetadata parent;

    public ScalarMetadataImpl(RangeMetadata parent, String name,
            String description, Phenomenon parameter, Unit units, Class<?> clazz) {
        this.parent = parent;
        this.name = name;
        this.description = description;
        this.parameter = parameter;
        this.units = units;
        this.clazz = clazz;
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
    public Class<?> getValueType() {
        return this.clazz;
    }

    @Override
    public Set<String> getMemberNames() {
        return Collections.emptySet();
    }

    @Override
    public RangeMetadata getParent() { return this.parent; }
    
    
    // TODO equals() and hashCode()
}
