/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

/**
 * A component of a vector field.  Will usually be part of a {@link VectorMetadata}
 * parent type.
 * @author Jon
 */
public interface VectorComponent<N extends Number> extends ScalarMetadata<N> {
    
    /**
     * Returns an identifier indicating the direction of the vector component.
     * @todo Use a stronger type here (e.g. enumeration or code list).
     */
    public String getDirection();
    
    @Override
    public VectorMetadata getParent();
    
}
