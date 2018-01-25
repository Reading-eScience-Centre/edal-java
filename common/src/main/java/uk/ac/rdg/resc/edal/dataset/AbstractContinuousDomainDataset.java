/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.exceptions.VariableNotFoundException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Partial implementation of a {@link Dataset} with a continuous domain which
 * performs spatial indexing of features.
 * 
 * @author Guy Griffiths
 */
public abstract class AbstractContinuousDomainDataset extends AbstractDataset implements ContinuousDomainDataset {
    private static final long serialVersionUID = 1L;
    protected FeatureIndexer featureIndexer;

    public AbstractContinuousDomainDataset(String id, Collection<? extends VariableMetadata> vars,
            FeatureIndexer featureIndexer) {
        super(id, vars);
        this.featureIndexer = featureIndexer;
    }

    @Override
    public DiscreteFeature<?, ?> readFeature(String featureId) throws DataReadingException {
        return getFeatureReader().readFeature(featureId, null);
    }

    @Override
    public Set<String> getFeatureIds() {
        return featureIndexer.getAllFeatureIds();
    }

    /**
     * Extracts features to be plotted on a map.
     * 
     * @param varIds
     *            The IDs of the variables to be extracted. If this is
     *            <code>null</code> then all variable IDs will be plotted. Any
     *            non-scalar parent variables will have all of their child
     *            variables extracted.
     * @param hExtent
     *            The {@link BoundingBox} describing the horizontal domain from
     *            which to extract features
     * @param zExtent
     *            The vertical extent from which to extract features
     * @param tExtent
     *            The time range from which to extract features
     * @return A {@link Collection} of {@link DiscreteFeature}s which can be
     *         plotted
     * @throws DataReadingException
     *             If there is a problem reading the underlying data
     * @throws VariableNotFoundException
     */
    @Override
    public List<? extends DiscreteFeature<?, ?>> extractMapFeatures(Set<String> varIds,
            BoundingBox hExtent, Extent<Double> zExtent, Double targetZ, Extent<DateTime> tExtent,
            DateTime targetT) throws DataReadingException {
        if (hExtent == null) {
            hExtent = getDatasetBoundingBox();
        }
        if (zExtent == null) {
            zExtent = getDatasetVerticalExtent();
        }
        if (tExtent == null) {
            tExtent = getDatasetTimeExtent();
        }
        List<DiscreteFeature<?, ?>> features = new ArrayList<>();
        Collection<String> featureIds = featureIndexer.findFeatureIds(hExtent, zExtent, tExtent,
                varIds);
        features.addAll(getFeatureReader().readFeatures(featureIds, varIds));
        return features;
    }

    @Override
    public List<? extends ProfileFeature> extractProfileFeatures(Set<String> varIds,
            BoundingBox bbox, Extent<Double> zExtent, Extent<DateTime> tExtent,
            final HorizontalPosition targetPos, DateTime targetTime)
            throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
        for (String varId : varIds) {
            if (!supportsProfileFeatureExtraction(varId)) {
                throw new UnsupportedOperationException(
                        "This dataset does not support profile features");
            }
        }
        List<ProfileFeature> features = new ArrayList<ProfileFeature>();

        if (bbox == null) {
            if (targetPos != null) {
                bbox = new BoundingBoxImpl(targetPos.getX(), targetPos.getY(), targetPos.getX(),
                        targetPos.getY(), targetPos.getCoordinateReferenceSystem());
            } else {
                bbox = getDatasetBoundingBox();
            }
        }
        if (tExtent == null) {
            if (targetTime != null) {
                tExtent = Extents.newExtent(targetTime, targetTime);
            } else {
                tExtent = getDatasetTimeExtent();
            }
        }
        Collection<String> featureIds = featureIndexer.findFeatureIds(bbox, zExtent, tExtent,
                varIds);
        @SuppressWarnings("unchecked")
        Collection<? extends ProfileFeature> readFeatures = (Collection<? extends ProfileFeature>) getFeatureReader()
                .readFeatures(featureIds, varIds);
        features.addAll(readFeatures);

        if (targetPos != null) {
            Collections.sort(features, new Comparator<ProfileFeature>() {
                @Override
                public int compare(ProfileFeature o1, ProfileFeature o2) {
                    return Double.compare(
                            GISUtils.getDistSquared(o1.getHorizontalPosition(), targetPos),
                            GISUtils.getDistSquared(o2.getHorizontalPosition(), targetPos));
                }
            });
        }
        return features;
    }

    @Override
    public List<? extends PointSeriesFeature> extractTimeseriesFeatures(Set<String> varIds,
            BoundingBox bbox, Extent<Double> zExtent, Extent<DateTime> tExtent,
            final HorizontalPosition targetPos, Double targetZ)
            throws DataReadingException, UnsupportedOperationException, VariableNotFoundException {
        for (String varId : varIds) {
            if (!supportsTimeseriesExtraction(varId)) {
                throw new UnsupportedOperationException(
                        "This dataset does not support time series features");
            }
        }
        List<PointSeriesFeature> features = new ArrayList<PointSeriesFeature>();

        if (bbox == null) {
            if (targetPos != null) {
                bbox = new BoundingBoxImpl(targetPos.getX(), targetPos.getY(), targetPos.getX(),
                        targetPos.getY(), targetPos.getCoordinateReferenceSystem());
            } else {
                bbox = getDatasetBoundingBox();
            }
        }
        if (zExtent == null) {
            if (targetZ != null) {
                zExtent = Extents.newExtent(targetZ, targetZ);
            } else {
                zExtent = getDatasetVerticalExtent();
            }
        }

        Collection<String> featureIds = featureIndexer.findFeatureIds(bbox, zExtent, tExtent,
                varIds);
        @SuppressWarnings("unchecked")
        Collection<? extends PointSeriesFeature> readFeatures = (Collection<? extends PointSeriesFeature>) getFeatureReader()
                .readFeatures(featureIds, varIds);
        features.addAll(readFeatures);
        if (targetPos != null) {
            Collections.sort(features, new Comparator<PointSeriesFeature>() {
                @Override
                public int compare(PointSeriesFeature o1, PointSeriesFeature o2) {
                    return Double.compare(
                            GISUtils.getDistSquared(o1.getHorizontalPosition(), targetPos),
                            GISUtils.getDistSquared(o2.getHorizontalPosition(), targetPos));
                }
            });
        }
        return features;
    }

    /**
     * @return The {@link BoundingBox} of the entire dataset
     */
    protected abstract BoundingBox getDatasetBoundingBox();

    /**
     * @return The {@link Extent} of the vertical domain for the entire dataset
     */
    protected abstract Extent<Double> getDatasetVerticalExtent();

    /**
     * @return The {@link Extent} of the time domain for the entire dataset
     */
    protected abstract Extent<DateTime> getDatasetTimeExtent();

    public abstract DiscreteFeatureReader<? extends DiscreteFeature<?, ?>> getFeatureReader();
}
