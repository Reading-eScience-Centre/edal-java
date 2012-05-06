/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

import java.util.Set;
import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;

/**
 * Descriptor for a scalar field
 * @param <T> The type of the values of the scalar field.  This will usually be
 * a number, but may be anything at all (e.g. a String), provided it can be
 * considered a "simple" type with no sub-components or children.
 * @author Jon
 */
public interface ScalarMetadata<T> extends RangeMetadata {
    
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
    
}
