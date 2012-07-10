/*******************************************************************************
 * Copyright (c) 2012 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.coverage.metadata;

import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.Coverage;

/**
 * Holds metadata for the range of a {@link Coverage}. Allows for nested
 * metadata elements.
 * 
 * @author Jon
 */
public interface RangeMetadata extends Cloneable {

    /**
     * Returns the identifier of this element, unique within the whole metadata
     * hierarchy.
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
     * the memberName does not exist. Always returns null if there are no child
     * members.
     * 
     * @param name
     *            the member name of the child.
     */
    public RangeMetadata getMemberMetadata(String name);

    /**
     * Returns the parent metadata, or null if this is the top-level metadata
     * object.
     */
    public RangeMetadata getParent();

    /**
     * Adds a member to this {@link RangeMetadata}
     * 
     * @param metadata
     * @throws IllegalArgumentException
     *             if metadata is null, or a member with this name already
     *             exists within this {@link RangeMetadata}
     */
    public void addMember(RangeMetadata metadata);

    /**
     * Removes a member from the metadata tree. This method should only remove a
     * top-level child member of this {@link RangeMetadata}.
     * 
     * @param memberName
     *            the member to remove
     * @throws IllegalArgumentException
     *             if the member is not a direct child of this metadata
     * @return the removed member
     */
    public RangeMetadata removeMember(String memberName);
    
    /**
     * Overrides the clone() method.  This is convenient for extracting
     * sub-features which share metadata.
     */
    public RangeMetadata clone() throws CloneNotSupportedException;
}
