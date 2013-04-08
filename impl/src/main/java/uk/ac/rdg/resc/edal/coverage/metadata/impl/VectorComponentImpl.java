package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorComponent;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorMetadata;

public class VectorComponentImpl extends ScalarMetadataImpl implements VectorComponent {

    private VectorComponentType direction;

    public VectorComponentImpl(String name, String description,
            Phenomenon parameter, Unit units, Class<?> clazz, VectorComponentType direction) {
        super(name, description, parameter, units, clazz);
        this.direction = direction;
    }
    
    @Override
    public VectorMetadata getParent() {
        return (VectorMetadata) super.getParent();
    }

    @Override
    public VectorComponentType getComponentType() {
        return direction;
    }

    @Override
    public void setParentMetadata(RangeMetadata parent) {
        if(!(parent instanceof VectorMetadata)) {
            throw new IllegalArgumentException("Parent metadata of a VectorComponent must be a VectorMetadata");
        }
        super.setParentMetadata(parent);
    }
    
    @Override
    public VectorComponent clone() throws CloneNotSupportedException {
        return new VectorComponentImpl(getName(), getDescription(), getParameter(),
                getUnits(), getValueType(), direction);
    }
}
