/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

import java.util.Set;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.Coverage;

/**
 * Descriptor for a scalar field
 * @author Jon
 */
public interface ScalarMetadata extends RangeMetadata {
    
    /**
     * Always returns the empty set: descriptors of scalars can have no
     * children.
     */
    @Override
    public Set<String> getMemberNames();
    
    /**
     * Always returns null: descriptors of scalars can have no
     * children.
     */
    @Override
    public RangeMetadata getMemberMetadata(String memberName);
    
    /**
     * Returns the units of measure.
     */
    public Unit getUnits();
    
    /**
     * Returns the identifier of the quantity being measured.
     */
    public Phenomenon getParameter();
    
    /**
     * Returns the runtime type of the values of the scalar. (This is also
     * exposed through {@link Coverage#getRangeType()}.)
     */
    public Class<?> getValueType();
    
}
