/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

/**
 * Metadata that holds a number of {@link VectorComponent}s, together describing
 * a vector quantity.
 * @author Jon
 */
public interface VectorMetadata extends RangeMetadata {
    
    /**
     * Returns the metadata descriptor for the given vector component
     */
    @Override
    public VectorComponent getMemberMetadata(String componentName);
    
}
