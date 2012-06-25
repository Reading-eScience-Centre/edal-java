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

package uk.ac.rdg.resc.edal.coverage.plugins;

import java.util.Arrays;
import java.util.List;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.PhenomenonVocabulary;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.UnitVocabulary;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractMultimemberDiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.ScalarMetadataImpl;

/**
 * A {@link Plugin} which takes two members and replaces them with X, Y, MAG,
 * DIR - the two original components, and their magnitude and direction when
 * treated as a vector. Add this to an instance of
 * {@link AbstractMultimemberDiscreteCoverage} to have some of its members
 * automatically recognised as vectors
 * 
 * @author Guy Griffiths
 * 
 */
public class VectorPlugin extends Plugin {

    private final String commonStandardName;

    /**
     * Instantiate a new {@link VectorPlugin}
     * 
     * @param xCompId
     *            the textual identifier of the x-component
     * @param yCompId
     *            the textual identifier of the y-component
     * @param commonStandardName
     *            the common part of their standard name:
     * 
     *            e.g. for components with standard names
     *            eastward_sea_water_velocity and northward_sea_water_velocity,
     *            the common part of the standard name is sea_water_velocity
     * 
     * @param description
     *            a description of the new {@link RangeMetadata}
     */
    public VectorPlugin(String xCompId, String yCompId, String commonStandardName,
            String description) {
        super(Arrays.asList(xCompId, yCompId), Arrays.asList("X", "Y", "MAG", "DIR"), description);
        this.commonStandardName = commonStandardName;
    }

    @Override
    protected Object generateValue(String component, List<Object> values) {
        if ("X".equals(component)) {
            return values.get(0);
        } else if ("Y".equals(component)) {
            return values.get(1);
        } else if ("MAG".equals(component)) {
            return (float) Math.sqrt(Math.pow((Float) values.get(0), 2)
                    + Math.pow((Float) values.get(1), 2));
        } else if ("DIR".equals(component)) {
            return (float) Math.atan2((Float) values.get(1), (Float) values.get(0));
        } else {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + component);
        }
    }

    private RangeMetadata xMetadata = null;
    private RangeMetadata yMetadata = null;
    private RangeMetadata magMetadata = null;
    private RangeMetadata dirMetadata = null;

    @Override
    protected RangeMetadata generateMetadata(String component, List<ScalarMetadata> metadataList,
            RangeMetadata parentMetadata) {
        if ("X".equals(component)) {
            if (xMetadata == null) {
                ScalarMetadata sMetadata = metadataList.get(0);
                xMetadata = new ScalarMetadataImpl(parentMetadata, getParentName() + "_X",
                        sMetadata.getDescription(), sMetadata.getParameter(), sMetadata.getUnits(),
                        sMetadata.getValueType());
            }
            return xMetadata;
        } else if ("Y".equals(component)) {
            if (yMetadata == null) {
                ScalarMetadata sMetadata = metadataList.get(1);
                yMetadata = new ScalarMetadataImpl(parentMetadata, getParentName() + "_Y",
                        sMetadata.getDescription(), sMetadata.getParameter(), sMetadata.getUnits(),
                        sMetadata.getValueType());
            }
            return yMetadata;
        } else if ("MAG".equals(component)) {
            if (magMetadata == null) {
                ScalarMetadata xComponentMetadata = metadataList.get(0);
                ScalarMetadata yComponentMetadata = metadataList.get(1);
                magMetadata = new ScalarMetadataImpl(parentMetadata, getParentName() + "_MAG",
                        "Magnitude of (" + xComponentMetadata.getDescription() + ", "
                                + yComponentMetadata.getDescription() + ")",
                        Phenomenon.getPhenomenon(
                                commonStandardName.replaceFirst("velocity", "speed"),
                                PhenomenonVocabulary.CLIMATE_AND_FORECAST),
                        xComponentMetadata.getUnits(), xComponentMetadata.getValueType());
            }
            return magMetadata;
        } else if ("DIR".equals(component)) {
            if (dirMetadata == null) {
                ScalarMetadata xComponentMetadata = metadataList.get(0);
                ScalarMetadata yComponentMetadata = metadataList.get(1);
                dirMetadata = new ScalarMetadataImpl(parentMetadata, getParentName() + "_DIR",
                        "Direction of (" + xComponentMetadata.getDescription() + ", "
                                + yComponentMetadata.getDescription() + ")",
                        Phenomenon.getPhenomenon(
                                commonStandardName.replaceFirst("velocity", "direction"),
                                PhenomenonVocabulary.UNKNOWN), Unit.getUnit("rad",
                                UnitVocabulary.UDUNITS2), xComponentMetadata.getValueType());
            }
            return dirMetadata;
        } else {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + component);
        }
    }
}
