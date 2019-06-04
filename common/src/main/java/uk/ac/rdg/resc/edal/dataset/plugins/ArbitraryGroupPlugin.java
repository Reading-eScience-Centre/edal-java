/*******************************************************************************
 * Copyright (c) 2019 The University of Reading
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

package uk.ac.rdg.resc.edal.dataset.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * A plugin to create arbitrary groups of variables.
 *
 * @author Guy Griffiths
 */
public class ArbitraryGroupPlugin extends VariablePlugin {
    private static final Logger log = LoggerFactory.getLogger(ArbitraryGroupPlugin.class);

    public final static String GROUP = "group";
    private final String title;

    /**
     * Construct a new {@link ArbitraryGroupPlugin}
     *
     */
    public ArbitraryGroupPlugin(String title, String... components) {
        super(components, new String[0]);
        this.title = title;
    }

    @Override
    protected VariableMetadata[] doProcessVariableMetadata(VariableMetadata... metadata)
            throws EdalException {
        /*
         * Find the original parent for the metadata
         * 
         * TODO - Check that all metadata have the same parent?
         */
        VariableMetadata parentMetadata = metadata[0].getParent();

        /*
         * Create a new container metadata object
         */
        VariableMetadata containerMetadata = newVariableMetadataFromMetadata(
                new Parameter(getFullId(GROUP), title, "Group of " + title, null, null), false,
                metadata);

        /*
         * Set all group members to have the new parent, with their "role" being
         * the standard name.
         */
        for (VariableMetadata m : metadata) {
            m.setParent(containerMetadata, m.getParameter().getStandardName());
        }

        /*
         * Add the container to the original parent
         */
        containerMetadata.setParent(parentMetadata, null);

        /*
         * Return the newly-added VariableMetadata objects, as required
         */
        return new VariableMetadata[] { containerMetadata };
    }

    @Override
    protected Number generateValue(String varSuffix, HorizontalPosition position,
            Number... sourceValues) {
        /*
         * This group does not provide any values. This method should never be
         * called.
         */
        log.warn("Trying to generate value for group which doesn't supply any values.s");
        return null;
    }
}
