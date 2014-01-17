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
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.DataReadingException;
import uk.ac.rdg.resc.edal.feature.DiscreteFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

/**
 * Partial implementation of a {@link ContinuousDomainDataset} which performs
 * spatial indexing of features.
 * 
 * @author Guy Griffiths
 * 
 * @param <F>
 *            The type of {@link DiscreteFeature} contained in this
 *            {@link AbstractContinuousDomainDataset}
 */
public abstract class AbstractContinuousDomainDataset<F extends DiscreteFeature<?, ?>> extends
        AbstractDataset<F> implements ContinuousDomainDataset<F> {

    private Class<F> featureType;
    private FeatureIndexer featureIndexer;

    public AbstractContinuousDomainDataset(String id, Collection<? extends VariableMetadata> vars,
            Class<F> featureType, FeatureIndexer featureIndexer) {
        super(id, vars);
        this.featureType = featureType;
        this.featureIndexer = featureIndexer;
    }

    @Override
    public F readFeature(String featureId) throws DataReadingException {
        return getFeatureReader().readFeature(featureId, null);
    }

    @Override
    public Set<String> getFeatureIds() {
        return featureIndexer.getAllFeatureIds();
    }

    @Override
    public Collection<F> extractFeatures(Set<String> varIds, BoundingBox hExtent,
            Extent<Double> zExtent, Extent<DateTime> tExtent) throws DataReadingException {
        if (hExtent == null) {
            hExtent = getDatasetBoundingBox();
        }
        if (zExtent == null) {
            zExtent = getDatasetVerticalExtent();
        }
        if (tExtent == null) {
            tExtent = getDatasetTimeExtent();
        }
        BoundingBox largeBoundingBox = getLargeBoundingBox(hExtent, 5);
        List<F> features = new ArrayList<F>();
        Collection<String> featureIds = featureIndexer.findFeatureIds(largeBoundingBox, zExtent,
                tExtent, varIds);
        features.addAll(getFeatureReader().readFeatures(featureIds, varIds));
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

    @Override
    public Class<F> getFeatureType() {
        return featureType;
    }

    /**
     * Increases the size of a {@link BoundingBox} by a given factor
     * 
     * @param bbox
     *            The {@link BoundingBox} to increase the size of
     * @param percentageIncrease
     *            The percentage increase
     * @return A larger {@link BoundingBox} with the same centre
     */
    public static BoundingBox getLargeBoundingBox(BoundingBox bbox, double percentageIncrease) {
        /*
         * Divide by 200 because we these values get used twice (once on each side)
         */
        double xExtra = bbox.getWidth() * (1.0 + percentageIncrease / 200.0);
        double yExtra = bbox.getHeight() * (1.0 + percentageIncrease / 200.0);
        BoundingBox bboxBordered = new BoundingBoxImpl(bbox.getMinX() - xExtra, bbox.getMinY()
                - yExtra, bbox.getMaxX() + xExtra, bbox.getMaxY() + yExtra,
                bbox.getCoordinateReferenceSystem());
        return bboxBordered;
    }

    public abstract DiscreteFeatureReader<F> getFeatureReader();
}
