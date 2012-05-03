/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

import java.util.Set;
import uk.ac.rdg.resc.edal.coverage.Coverage;

/**
 * Holds metadata for the range of a {@link Coverage}.
 * Allows for nested metadata elements.
 * @author Jon
 */
public interface RangeMetadata {
    
    /**
     * Returns the identifier of this element, unique among its siblings, but
     * not necessarily unique within the whole metadata hierarchy.
     */
    public String getName();
    
    /**
     * Returns a human-readable description of the metadata.
     */
    public String getDescription();
    
    /**
     * Returns the set of identifiers of immediate child metadata members, or
     * the empty set if there are no child members.
     */
    public Set<String> getMemberNames();
    
    /**
     * Returns the metadata descriptor for the given child member, or null if
     * the memberName does not exist.  Always returns null if there are no
     * child members.
     * @param name the member name of the child.
     */
    public RangeMetadata getMemberMetadata(String name);
    
    /**
     * Returns the parent metadata, or null if this is the top-level metadata
     * object.
     */
    public RangeMetadata getParent();
    
}
