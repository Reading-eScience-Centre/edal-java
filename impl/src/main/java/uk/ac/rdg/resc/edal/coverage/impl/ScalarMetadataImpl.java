package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.Set;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

public class ScalarMetadataImpl implements ScalarMetadata {

    private final String description;
    private final Phenomenon parameter;
    private final Unit units;
    private final Class<?> clazz;

    public ScalarMetadataImpl(String description, Phenomenon parameter, Unit units, Class<?> clazz) {
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

    // TODO Check that this is the right behaviour...
    @Override
    public Class<?> getValueType() {
        return clazz;
    }

    @Override
    public RangeMetadata getMemberMetadata(String memberName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> getMemberNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RangeMetadata getParent() { return null; }
}
