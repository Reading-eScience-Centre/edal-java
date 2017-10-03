/*******************************************************************************
 * Copyright (c) 2017 The University of Reading
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * A {@link Dataset} which contains only {@link TrajectoryFeature}s. 
 *
 * @author Guy Griffiths
 */
public class TrajectoryDataset extends AbstractDataset {
    private static final long serialVersionUID = 1L;

    private DiscreteFeatureReader<TrajectoryFeature> featureReader;

    public TrajectoryDataset(String id, Collection<VariableMetadata> vars,
            DiscreteFeatureReader<TrajectoryFeature> featureReader) {
        super(id, vars);
        this.featureReader = featureReader;
    }

    @Override
    public Set<String> getFeatureIds() {
        return getVariableIds();
    }

    @Override
    public Class<? extends DiscreteFeature<?, ?>> getFeatureType(String variableId) {
        return TrajectoryFeature.class;
    }

    @Override
    public Class<? extends DiscreteFeature<?, ?>> getMapFeatureType(String variableId) {
        /*
         * Return TrajectoryFeatures for plotting on the map.
         */
        return TrajectoryFeature.class;
    }

    @Override
    public TrajectoryFeature readFeature(String featureId)
            throws DataReadingException, VariableNotFoundException {
        return featureReader.readFeature(featureId, vars.keySet());
    }

    public List<TrajectoryFeature> extractFeatures(Set<String> varIds, BoundingBox bbox,
            Extent<Double> zExtent, Extent<DateTime> tExtent) {
        List<TrajectoryFeature> features = new ArrayList<>();

        /*
         * 
         * TODO - Implement this. It needs to use a feature indexer, which
         * probably needs to be populated + injected into this class on
         * construction. Then search for all features which cross the bounding
         * box, to ensure that we can plot all entries/exits.
         * 
         */

        /*
         * Temporary - just reads a feature with a single ID, which is currently
         * equivalent to the variable ID. Won't be the case once feature
         * indexing has been added.
         */
        features.add(readFeature(varIds.iterator().next()));

        return features;
    }

    @Override
    public List<? extends ProfileFeature> extractProfileFeatures(Set<String> varIds,
            BoundingBox bbox, Extent<Double> zExtent, Extent<DateTime> tExtent,
            HorizontalPosition targetPos, DateTime targetTime)
            throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
        throw new UnsupportedOperationException("Profile extraction not supported");
    }

    @Override
    public boolean supportsProfileFeatureExtraction(String varId) {
        return false;
    }

    @Override
    public List<? extends PointSeriesFeature> extractTimeseriesFeatures(Set<String> varIds,
            BoundingBox bbox, Extent<Double> zExtent, Extent<DateTime> tExtent,
            HorizontalPosition targetPos, Double targetZ)
            throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
        throw new UnsupportedOperationException("Timeseries extraction not supported");
    }

    @Override
    public boolean supportsTimeseriesExtraction(String varId) {
        return false;
    }
}
