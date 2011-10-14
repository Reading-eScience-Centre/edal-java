package uk.ac.rdg.resc.edal.coverage;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;

public class RangeMetadataImpl implements RangeMetadata {

    private final String description;
    private final Phenomenon parameter;
    private final Unit units;
    private final Class<?> clazz;

    public RangeMetadataImpl(String description, Phenomenon parameter, Unit units, Class<?> clazz) {
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
}
