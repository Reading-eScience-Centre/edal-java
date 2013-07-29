/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

package uk.ac.rdg.resc.edal.feature;

import java.util.Set;
import uk.ac.rdg.resc.edal.metadata.Parameter;

/**
 * Contains the data values returned by a {@link Feature}.
 * 
 * @author Jon Blower
 */
public interface Record {
    /**
     * <p>
     * Gets the value of the given feature member..
     * </p>
     * 
     * @param paramId
     *            The name of a member of this record as provided by the
     *            Feature's {@link Feature#getParameterIds() set of parameter
     *            ids}.
     * @return the value of the given member.
     * @throws IllegalArgumentException
     *             if {@code paramId} is not a valid parameter Id
     */
    public Number getValue(String paramId);

    /**
     * Returns a Set of unique identifiers, one for each member of the coverage.
     * These identifiers are not (necessarily) intended to be human-readable.
     * 
     * @return a Set of unique identifiers, one for each member of the coverage.
     */
    public Set<String> getParameterIds();

    /**
     * Returns a description of the values returned by the coverage (including
     * their units and the phenomenon they represent) for a particular member.
     * 
     * @param paramId
     *            The unique identifier of the member
     * @return a description of the values returned by the coverage for a
     *         particular member.
     * @throws IllegalArgumentException
     *             if {@code paramId} is not present in the
     *             {@link #getParameterIds() set of member names}.
     */
    public Parameter getParameter(String paramId);
}
