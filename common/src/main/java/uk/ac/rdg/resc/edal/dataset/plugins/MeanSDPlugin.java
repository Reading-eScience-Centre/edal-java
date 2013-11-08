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

package uk.ac.rdg.resc.edal.dataset.plugins;

import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * A plugin to group mean and standard deviation of a single variable
 * 
 * @author Guy Griffiths
 */
public class MeanSDPlugin extends VariablePlugin {

    public final static String GROUP = "stats_group";
    private String title;

    /**
     * Construct a new {@link MeanSDPlugin}
     * 
     * @param meanComponentId
     *            The ID of the variable representing the x-component
     * @param sdComponentId
     *            The ID of the variable representing the y-component
     * @param title
     *            The title of the quantity which the components represent
     */
    public MeanSDPlugin(String meanComponentId, String sdComponentId, String title) {
        super(new String[] { meanComponentId, sdComponentId }, new String[] { GROUP });
        this.title = title;
    }

    @Override
    protected VariableMetadata[] doProcessVariableMetadata(VariableMetadata... metadata) {
        /*
         * We get the same components we supply in the constructor, so this is
         * safe.
         */
        VariableMetadata meanMetadata = metadata[0];
        VariableMetadata sdMetadata = metadata[1];

        /*
         * Get domains where both components are valid
         */
        HorizontalDomain hDomain = getIntersectionOfHorizontalDomains(
                meanMetadata.getHorizontalDomain(), sdMetadata.getHorizontalDomain());
        VerticalDomain vDomain = getIntersectionOfVerticalDomains(meanMetadata.getVerticalDomain(),
                sdMetadata.getVerticalDomain());
        TemporalDomain tDomain = getIntersectionOfTemporalDomains(meanMetadata.getTemporalDomain(),
                sdMetadata.getTemporalDomain());

        /*
         * Find the original parent which the mean component belongs to (and almost
         * certainly the sd component)
         */
        VariableMetadata parentMetadata = meanMetadata.getParent();

        /*
         * Create a new container metadata object
         */
        VariableMetadata containerMetadata = new VariableMetadata(getFullId(GROUP), new Parameter(
                getFullId(GROUP), title, "Statistics for " + title, null), hDomain, vDomain,
                tDomain, false);

        /*
         * Set all components to have a new parent
         */
        meanMetadata.setParent(containerMetadata, "mean");
        sdMetadata.setParent(containerMetadata, "stddev");

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
    protected Number generateValue(String varSuffix, Number... sourceValues) {
        /*
         * We are not generating new values with this plugin - it is just there
         * to group the mean and SD together. We have introduced a new grouping
         * variable, but this has no values.
         * 
         * That means that this method should never end up being called.
         * 
         * Even if it were called, it could only be validly called for the
         * grouping variable which has no values anyway. Therefore we just...
         */
        return null;
    }

}
