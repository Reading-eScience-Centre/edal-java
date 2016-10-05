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

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.domain.Domain;
import uk.ac.rdg.resc.edal.metadata.Parameter;

/**
 * <p>
 * Superclass for all Feature types.
 * </p>
 * 
 * @param <P>
 *            The type of object used to identify positions within the feature's
 *            domain. This may be a spatial, temporal, or combined
 *            spatiotemporal position.
 * @author Jon Blower
 */
public interface Feature<P> {
    /**
     * @return an identifier that is unique within the {@link Dataset} to which
     *         this feature belongs. Must never be <code>null</code>.
     */
    public String getId();

    /**
     * @return a human-readable short string that identifies this feature. Not
     *         enforced to be unique.
     */
    public String getName();

    /**
     * @return a (perhaps lengthy) human-readable description of this feature.
     */
    public String getDescription();

    /**
     * @return the domain of the values contained with the feature. May not
     *         return <code>null</code>
     */
    public Domain<P> getDomain();

    /**
     * @return the set of identifiers of the variables recorded in this Feature
     */
    public Set<String> getVariableIds();

    /**
     * Gets a {@link Parameter} associated with a variable in this
     * {@link Feature}
     * 
     * @param variableId
     *            The ID of the desired {@link Parameter}
     * @return the desired {@link Parameter}
     */
    public Parameter getParameter(String variableId);

    /**
     * @return the set of identifiers of the variables in this Feature mapped to
     *         the {@link Parameter}s themselves
     */
    public Map<String, Parameter> getParameterMap();

    /**
     * @return a {@link Properties} object containing an arbitrary list of
     *         {@link String} properties associated with this {@link Feature}.
     *         This can be used to attach any additional information to the
     *         {@link Feature} which doesn't fit elsewhere. Will not return
     *         <code>null</code>, but may be empty.
     */
    public Properties getFeatureProperties();
}
