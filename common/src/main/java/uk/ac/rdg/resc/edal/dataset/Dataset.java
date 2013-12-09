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

package uk.ac.rdg.resc.edal.dataset;

import java.util.Set;

import uk.ac.rdg.resc.edal.dataset.plugins.VariablePlugin;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * Provides access to data and metadata held in underlying storage,
 * {@literal e.g.} on disk, in a database or on a remote server.
 * 
 * @author Jon
 * @author Guy
 */
public interface Dataset {

    /**
     * @return The ID which identifies this dataset.
     */
    public String getId();

    /**
     * Returns the IDs of features which are present in this Dataset
     */
    public Set<String> getFeatureIds();

    /**
     * Reads an entire feature from underlying storage
     */
    public Feature<?> readFeature(String featureId) throws DataReadingException;

    /**
     * Returns the IDs of variables in this {@link Dataset}. Generally the term
     * "variable" refers to a measured physical quantity
     */
    public Set<String> getVariableIds();

    /**
     * Returns the {@link VariableMetadata} associated with a particular
     * variable ID
     * 
     * @param variableId
     *            The variable ID to search for
     * @return The desired {@link VariableMetadata}
     */
    public VariableMetadata getVariableMetadata(String variableId);

    /**
     * Returns the variables at the top level of the hierarchy.
     */
    public Set<VariableMetadata> getTopLevelVariables();

    /**
     * Adds a {@link VariablePlugin} to this dataset to generate derived
     * variables from existing ones in the {@link Dataset}
     * 
     * @param plugin
     *            The {@link VariablePlugin} to add
     * @throws EdalException
     *             If there is a problem adding the plugin
     */
    public void addVariablePlugin(VariablePlugin plugin) throws EdalException;
}
