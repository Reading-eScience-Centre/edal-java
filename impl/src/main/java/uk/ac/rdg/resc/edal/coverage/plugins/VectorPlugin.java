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

    @Override
    protected String generateDescription(String component, List<String> descriptions) {
        if ("X".equals(component)) {
            return descriptions.get(0);
        } else if ("Y".equals(component)) {
            return descriptions.get(1);
        } else if ("MAG".equals(component)) {
            return "Magnitude of (" + descriptions.get(0) + "," + descriptions.get(1) + ")";
        } else if ("DIR".equals(component)) {
            return "Direction of (" + descriptions.get(0) + "," + descriptions.get(1) + ")";
        } else {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + component);
        }
    }

    @Override
    protected Phenomenon generatePhenomenon(String component, List<Phenomenon> phenomenons) {
        if ("X".equals(component)) {
            return phenomenons.get(0);
        } else if ("Y".equals(component)) {
            return phenomenons.get(1);
        } else if ("MAG".equals(component)) {
            return Phenomenon.getPhenomenon(commonStandardName.replaceFirst("velocity", "speed"),
                    PhenomenonVocabulary.CLIMATE_AND_FORECAST);
        } else if ("DIR".equals(component)) {
            return Phenomenon.getPhenomenon(
                    commonStandardName.replaceFirst("velocity", "direction"),
                    PhenomenonVocabulary.UNKNOWN);
        } else {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + component);
        }
    }

    @Override
    protected Unit generateUnits(String component, List<Unit> unit) {
        if ("X".equals(component)) {
            return unit.get(0);
        } else if ("Y".equals(component)) {
            return unit.get(1);
        } else if ("MAG".equals(component)) {
            return unit.get(0);
        } else if ("DIR".equals(component)) {
            return Unit.getUnit("rad", UnitVocabulary.UDUNITS2);
        } else {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + component);
        }
    }

    @Override
    protected Class<?> generateValueType(String component, List<Class<?>> classes) {
        if ("X".equals(component)) {
            return classes.get(0);
        } else if ("Y".equals(component)) {
            return classes.get(1);
        } else if ("MAG".equals(component)) {
            return Float.class;
        } else if ("DIR".equals(component)) {
            return Float.class;
        } else {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + component);
        }
    }
}
