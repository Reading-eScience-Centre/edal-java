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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A class representing a spatial indexer for features.
 * 
 * @author Guy Griffiths
 */
public interface FeatureIndexer extends Serializable {

    /**
     * Finds the IDs of features with the given spatio-temporal constraints. If
     * any constraint is <code>null</code> it is considered to be unconstrained
     * in that dimension.
     * 
     * @param horizontalExtent
     *            The {@link BoundingBox} which features must have positions in.
     * @param verticalExtent
     *            The vertical range which features must have positions in.
     * @param timeExtent
     *            The time range which features must have positions in.
     * @param variableIds
     *            The variable IDs which features must contain
     * @return A {@link Collection} of IDs for features which match all of the
     *         given constraints
     */
    public Collection<String> findFeatureIds(BoundingBox horizontalExtent,
            Extent<Double> verticalExtent, Extent<DateTime> timeExtent,
            Collection<String> variableIds);

    /**
     * @return All feature IDs present in this {@link FeatureIndexer}
     */
    public Set<String> getAllFeatureIds();

    /**
     * Adds features to this indexer. Features are defined in terms of
     * {@link FeatureBounds} objects which define the spatial boundaries of
     * point features
     * 
     * @param features
     */
    public void addFeatures(List<FeatureBounds> features);

    /**
     * A class representing the spatial bounds of a feature with a single point.
     * This is used to index features.
     */
    public static class FeatureBounds implements Serializable {
        private static final long serialVersionUID = 1L;
        String id;
        HorizontalPosition horizontalPosition;
        Extent<Double> verticalExtent;
        Extent<Long> timeExtent;
        Collection<String> variableIds;

        public FeatureBounds(String id, HorizontalPosition horizontalPosition,
                Extent<Double> verticalExtent, Extent<DateTime> timeExtent,
                Collection<String> variableIds) {
            super();
            this.id = id;
            this.horizontalPosition = horizontalPosition;
            this.verticalExtent = verticalExtent;
            this.timeExtent = Extents.newExtent(timeExtent.getLow().getMillis(), timeExtent
                    .getHigh().getMillis());
            this.variableIds = variableIds;
        }
    }
}
