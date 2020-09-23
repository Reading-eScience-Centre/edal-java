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
 * A plugin to group the value of a single variable with an associated error /
 * uncertainty
 * 
 * @author Guy Griffiths
 */
public class ValueErrorPlugin extends VariablePlugin {

    public final static String VALUE_ROLE = "value";
    public final static String ERROR_ROLE = "error";
    public final static String UPPER_ROLE = "upperbound";
    public final static String LOWER_ROLE = "lowerbound";

    public final static String GROUP = "uncertainty_group";
    private String title;

    /**
     * Construct a new {@link ValueErrorPlugin}
     * 
     * @param valueComponentId
     *            The ID of the variable representing the x-component
     * @param errorComponentId
     *            The ID of the variable representing the y-component
     * @param title
     *            The title of the quantity which the components represent
     */
    public ValueErrorPlugin(String valueComponentId, String errorComponentId, String title) {
        super(new String[] { valueComponentId, errorComponentId }, new String[] { UPPER_ROLE,
                LOWER_ROLE, GROUP });
        this.title = title;
    }

    @Override
    protected VariableMetadata[] doProcessVariableMetadata(VariableMetadata... metadata) {
        /*
         * We get the same components we supply in the constructor, so this is
         * safe.
         */
        VariableMetadata valueMetadata = metadata[0];
        VariableMetadata errorMetadata = metadata[1];

        /*
         * Find the original parent which the value component belongs to (and
         * almost certainly the error component)
         */
        VariableMetadata parentMetadata = valueMetadata.getParent();

        /*
         * Create new metadata for the upper and lower bounds
         */
        VariableMetadata upperMetadata = newVariableMetadataFromMetadata(new Parameter(
                getFullId(UPPER_ROLE), title + " upper bound", "The upper error bound of " + title
                        + " i.e. value + error", valueMetadata.getParameter().getUnits(),
                valueMetadata.getParameter().getStandardName()), true, valueMetadata, errorMetadata);

        VariableMetadata lowerMetadata = newVariableMetadataFromMetadata(new Parameter(
                getFullId(LOWER_ROLE), title + " lower bound", "The lower error bound of " + title
                        + " i.e. value - error", valueMetadata.getParameter().getUnits(),
                valueMetadata.getParameter().getStandardName()), true, valueMetadata, errorMetadata);

        /*
         * Create a new container metadata object
         */
        VariableMetadata containerMetadata = newVariableMetadataFromMetadata(new Parameter(
                getFullId(GROUP), title, "Statistics for " + title, null, null), false,
                valueMetadata, errorMetadata);

        /*
         * Set all components to have a new parent
         */
        valueMetadata.setParent(containerMetadata, VALUE_ROLE);
        errorMetadata.setParent(containerMetadata, ERROR_ROLE);
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
        /*-
         * We override this, such that the combined ID is just the ID of the value component.
         * 
         * That way we will have things like:
         * quantity-stats_group
         * quantity-upperbound
         * quantity-lowerbound
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
