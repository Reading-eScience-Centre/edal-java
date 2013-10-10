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
 * A plugin to generate magnitude and direction fields from x- and y-components,
 * and to group them logically.
 * 
 * @author Guy Griffiths
 */
public class VectorPlugin extends VariablePlugin {

    public final static String MAG = "mag";
    public final static String DIR = "dir";
    private String title;

    /**
     * Construct a new {@link VectorPlugin}
     * 
     * @param xComponentId
     *            The ID of the variable representing the x-component
     * @param yComponentId
     *            The ID of the variable representing the y-component
     * @param title
     *            The title of the quantity which the components represent
     */
    public VectorPlugin(String xComponentId, String yComponentId, String title) {
        super(new String[] { xComponentId, yComponentId }, new String[] { MAG, DIR });
        this.title = title;
    }

    @Override
    protected VariableMetadata[] doProcessVariableMetadata(VariableMetadata... metadata) {
        /*
         * We get the same components we supply in the constructor, so this is
         * safe.
         */
        VariableMetadata xMetadata = metadata[0];
        VariableMetadata yMetadata = metadata[1];

        /*
         * Get domains where both components are valid
         */
        HorizontalDomain hDomain = getUnionOfHorizontalDomains(xMetadata.getHorizontalDomain(),
                yMetadata.getHorizontalDomain());
        VerticalDomain vDomain = getUnionOfVerticalDomains(xMetadata.getVerticalDomain(),
                yMetadata.getVerticalDomain());
        TemporalDomain tDomain = getUnionOfTemporalDomains(xMetadata.getTemporalDomain(),
                yMetadata.getTemporalDomain());

        /*
         * Generate metadata for new components
         */
        VariableMetadata magMetadata = new VariableMetadata(getFullId(MAG), new Parameter(
                getFullId(MAG), "Magnitude of " + title, "Magnitude of components:\n"
                        + xMetadata.getParameter().getDescription() + " and\n"
                        + yMetadata.getParameter().getDescription(), xMetadata.getParameter()
                        .getUnits()), hDomain, vDomain, tDomain);
        VariableMetadata dirMetadata = new VariableMetadata(getFullId(DIR), new Parameter(
                getFullId(DIR), "Direction of " + title, "Direction of components:\n"
                        + xMetadata.getParameter().getDescription() + " and\n"
                        + yMetadata.getParameter().getDescription(), "degrees"), hDomain, vDomain,
                tDomain);

        /*
         * Find the original parent which the x-component belongs to (and almost
         * certainly the y-component)
         */
        VariableMetadata parentMetadata = xMetadata.getParent();

        /*
         * Create a new container metadata object
         */
        VariableMetadata containerMetadata = new VariableMetadata(getFullId("-group"),
                new Parameter(getFullId("-group"), title, "Vector fields for " + title, null),
                hDomain, vDomain, tDomain, true);

        /*
         * Set all components to have a new parent
         */
        xMetadata.setParent(containerMetadata);
        yMetadata.setParent(containerMetadata);
        magMetadata.setParent(containerMetadata);
        dirMetadata.setParent(containerMetadata);

        /*
         * Add the container to the original parent
         */
        containerMetadata.setParent(parentMetadata);

        /*
         * Return the newly-added VariableMetadata objects, as required
         */
        return new VariableMetadata[] { containerMetadata, magMetadata, dirMetadata };
    }

    @Override
    protected Number generateValue(String varSuffix, Number... sourceValues) {
        if (MAG.equals(varSuffix)) {
            return Math.sqrt(sourceValues[0].doubleValue() * sourceValues[0].doubleValue()
                    + sourceValues[1].doubleValue() * sourceValues[1].doubleValue());
        } else if (DIR.equals(varSuffix)) {
            return Math.atan2(sourceValues[1].doubleValue(), sourceValues[0].doubleValue());
        } else {
            /*
             * Should never get here.
             */
            assert false;
            return null;
        }
    }

}
