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
import uk.ac.rdg.resc.edal.coverage.impl.AbstractMultimemberDiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.Statistic;
import uk.ac.rdg.resc.edal.coverage.metadata.Statistic.StatisticType;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.StatisticCollectionImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.StatisticImpl;

/**
 * A {@link Plugin} which takes two members and replaces them with MEAN, VAR,
 * UPPER, LOWER - the two original components, and the upper and lower bounds
 * Add this to an instance of {@link AbstractMultimemberDiscreteCoverage} to
 * have some of its members automatically recognised as mean/variance pairs
 * 
 * @author Guy Griffiths
 * 
 */
public class MeanStddevPlugin extends Plugin {

    private final String description;
    private final String meanName;
    private final String stddevName;
    private final String upperBoundName;
    private final String lowerBoundName;
    private Phenomenon parentPhenomenon;

    /**
     * Instantiate a new {@link MeanStddevPlugin}
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
    public MeanStddevPlugin(ScalarMetadata meanMetadata, ScalarMetadata stddevMetadata,
            String description, Phenomenon parentPhenomenon) {
        super(Arrays.asList((RangeMetadata) meanMetadata, (RangeMetadata) stddevMetadata));
        this.description = description;
        this.meanName = getParentName() + "_MEAN";
        this.stddevName = getParentName() + "_VAR";
        this.upperBoundName = getParentName() + "_UPPER_BOUND";
        this.lowerBoundName = getParentName() + "_LOWER_BOUND";
        this.parentPhenomenon = parentPhenomenon;
    }

    @Override
    protected Object generateValue(String component, List<Object> values) {
        if (meanName.equals(component)) {
            return values.get(0);
        } else if (stddevName.equals(component)) {
            return values.get(1);
        } else if (upperBoundName.equals(component)) {
            return (Float) values.get(0) + 2f*(Float)values.get(1);
        } else if (lowerBoundName.equals(component)) {
            return (Float) values.get(0) - 2f*(Float)values.get(1);
        } else {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + component);
        }
    }

    @Override
    protected Class<?> generateValueType(String component, List<Class<?>> classes) {
        if (meanName.equals(component)) {
            return classes.get(0);
        } else if (stddevName.equals(component)) {
            return classes.get(1);
        } else if (upperBoundName.equals(component)) {
            return Float.class;
        } else if (lowerBoundName.equals(component)) {
            return Float.class;
        } else {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + component);
        }
    }
    
    private Statistic meanMetadata = null;
    private Statistic stddevMetadata = null;
    private Statistic upperBoundMetadata = null;
    private Statistic lowerBoundMetadata = null;
    
    @Override
    protected RangeMetadata generateRangeMetadata(List<RangeMetadata> metadataList) {
        /*
         * The casts to ScalarMetadata are fine, because
         */
        StatisticCollectionImpl metadata = new StatisticCollectionImpl(getParentName(), description, parentPhenomenon);
        if (meanMetadata == null) {
            ScalarMetadata sMetadata = (ScalarMetadata) metadataList.get(0);
            meanMetadata = new StatisticImpl(meanName, sMetadata.getDescription(),
                    sMetadata.getParameter(), sMetadata.getUnits(), sMetadata.getValueType(),
                    StatisticType.MEAN);
        }
        if (stddevMetadata == null) {
            ScalarMetadata sMetadata = (ScalarMetadata) metadataList.get(1);
            stddevMetadata = new StatisticImpl(stddevName, sMetadata.getDescription(),
                    sMetadata.getParameter(), sMetadata.getUnits(), sMetadata.getValueType(),
                    StatisticType.STANDARD_DEVIATION);
        }
        if (upperBoundMetadata == null) {
            ScalarMetadata meanMetadata = (ScalarMetadata) metadataList.get(0);
            String description = "2σ upper confidence of " + meanMetadata.getDescription();
            
            upperBoundMetadata = new StatisticImpl(upperBoundName, description,
                    meanMetadata.getParameter(), meanMetadata.getUnits(),
                    meanMetadata.getValueType(), StatisticType.UPPER_CONFIDENCE_BOUND);
        }
        if (lowerBoundMetadata == null) {
            ScalarMetadata meanMetadata = (ScalarMetadata) metadataList.get(0);
            String description = "2σ lower confidence of " + meanMetadata.getDescription();
            
            lowerBoundMetadata = new StatisticImpl(lowerBoundName, description,
                    meanMetadata.getParameter(), meanMetadata.getUnits(),
                    meanMetadata.getValueType(), StatisticType.LOWER_CONFIDENCE_BOUND);
        }
        metadata.addMember(meanMetadata);
        metadata.addMember(stddevMetadata);
        metadata.addMember(upperBoundMetadata);
        metadata.addMember(lowerBoundMetadata);
        
        metadata.setChildrenToPlot(Arrays.asList(meanMetadata.getName(), stddevMetadata.getName()));
        return metadata;
    }

    @Override
    protected ScalarMetadata getScalarMetadata(String memberName) {
        if (meanName.equals(memberName)) {
            return meanMetadata;
        } else if (stddevName.equals(memberName)) {
            return stddevMetadata;
        } else if (upperBoundName.equals(memberName)) {
            return upperBoundMetadata;
        } else if (lowerBoundName.equals(memberName)) {
            return lowerBoundMetadata;
        } else {
            throw new IllegalArgumentException(memberName + " is not provided by this plugin");
        }
    }
}
