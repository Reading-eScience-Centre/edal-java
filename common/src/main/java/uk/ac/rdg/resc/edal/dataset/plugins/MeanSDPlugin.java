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

import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * A plugin to group mean and standard deviation of a single variable
 * 
 * @author Guy Griffiths
 */
public class MeanSDPlugin extends VariablePlugin {

    public final static String MEAN_ROLE = "mean";
    public final static String STDDEV_ROLE = "stddev";
    public final static String UPPER_ROLE = "upperbound";
    public final static String LOWER_ROLE = "lowerbound";

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
        super(new String[] { meanComponentId, sdComponentId }, new String[] { UPPER_ROLE,
                LOWER_ROLE, GROUP });
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
         * Find the original parent which the mean component belongs to (and
         * almost certainly the sd component)
         */
        VariableMetadata parentMetadata = meanMetadata.getParent();

        /*
         * Create new metadata for the upper and lower bounds
         */
        VariableMetadata upperMetadata = newVariableMetadataFromMetadata(new Parameter(
                getFullId(UPPER_ROLE), title + " upper bound", "The upper error bound of " + title
                        + " i.e. mean + 1 std dev", meanMetadata.getParameter().getUnits(),
                meanMetadata.getParameter().getStandardName()), true, meanMetadata, sdMetadata);

        VariableMetadata lowerMetadata = newVariableMetadataFromMetadata(new Parameter(
                getFullId(LOWER_ROLE), title + " lower bound", "The lower error bound of " + title
                        + " i.e. mean - 1 std dev", meanMetadata.getParameter().getUnits(),
                meanMetadata.getParameter().getStandardName()), true, meanMetadata, sdMetadata);

        /*
         * Create a new container metadata object
         */
        VariableMetadata containerMetadata = newVariableMetadataFromMetadata(new Parameter(
                getFullId(GROUP), title, "Statistics for " + title, null, null), false,
                meanMetadata, sdMetadata);

        /*
         * Set all components to have a new parent
         */
        meanMetadata.setParent(containerMetadata, MEAN_ROLE);
        sdMetadata.setParent(containerMetadata, STDDEV_ROLE);
        upperMetadata.setParent(containerMetadata, UPPER_ROLE);
        lowerMetadata.setParent(containerMetadata, LOWER_ROLE);

        /*
         * Add the container to the original parent
         */
        containerMetadata.setParent(parentMetadata, null);

        /*
         * Return the newly-added VariableMetadata objects, as required
         */
        return new VariableMetadata[] { upperMetadata, lowerMetadata, containerMetadata };
    }

    @Override
    protected String combineIds(String... partsToUse) {
        /*
         * We override this, such that the combined ID is just the mean field.
         * 
         * That way we will have things like:
         * 
         * mean_quantity-stats_group
         * mean_quantity-upperbound
         * mean_quantity-lowerbound
         */
        return partsToUse[0];
    }

    @Override
    protected Number generateValue(String varSuffix, HorizontalPosition pos, Number... sourceValues) {
        /*
         * We only generate new values for the upper and lower bounds
         */
        if (UPPER_ROLE.equals(varSuffix)) {
            Number meanValue = sourceValues[0];
            Number sdValue = sourceValues[1];
            if (meanValue != null && sdValue != null) {
                return meanValue.doubleValue() + sdValue.doubleValue();
            }
        } else if (LOWER_ROLE.equals(varSuffix)) {
            Number meanValue = sourceValues[0];
            Number sdValue = sourceValues[1];
            if (meanValue != null && sdValue != null) {
                return meanValue.doubleValue() - sdValue.doubleValue();
            }
        }
        return null;
    }

}
