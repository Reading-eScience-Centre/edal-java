package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import java.util.Collections;
import java.util.Set;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

/**
 * An implementation of {@link ScalarMetadata}
 * 
 * @author Jon
 * @author Guy Griffiths
 * 
 */
public class ScalarMetadataImpl implements ScalarMetadata {

    private final String name;
    private final String description;
    private final Phenomenon parameter;
    private final Unit units;
    private final Class<?> clazz;
    private final RangeMetadata parent;

    public ScalarMetadataImpl(RangeMetadata parent, String name, String description,
            Phenomenon parameter, Unit units, Class<?> clazz) {
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
    public RangeMetadata getParent() {
        return this.parent;
    }

    @Override
    public RangeMetadata removeMember(String memberName) {
        throw new UnsupportedOperationException(
                "This is scalar metadata, and cannot have child members.  Therefore removing members is unsupported");
    }

    @Override
    public void addMember(RangeMetadata metadata) {
        throw new UnsupportedOperationException(
                "This is scalar metadata, and cannot have child members.  Therefore adding members is unsupported");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((units == null) ? 0 : units.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScalarMetadataImpl other = (ScalarMetadataImpl) obj;
        if (clazz == null) {
            if (other.clazz != null)
                return false;
        } else if (!clazz.equals(other.clazz))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parameter == null) {
            if (other.parameter != null)
                return false;
        } else if (!parameter.equals(other.parameter))
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (units == null) {
            if (other.units != null)
                return false;
        } else if (!units.equals(other.units))
            return false;
        return true;
    }
}
