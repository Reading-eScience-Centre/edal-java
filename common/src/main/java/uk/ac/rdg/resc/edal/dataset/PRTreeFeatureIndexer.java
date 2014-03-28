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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;

/**
 * This stores all feature bounds in memory and then exhaustively searches them
 * to find features.
 * 
 * That may sound like a terrible method of spatial indexing, but what do you
 * expect from a class named "NaiveFeatureIndexer"? This is really a placeholder
 * until something better is written.
 * 
 * @author Guy
 */
public class PRTreeFeatureIndexer implements FeatureIndexer,
        MBRConverter<FeatureIndexer.FeatureBounds> {

    private PRTree<FeatureBounds> prTree;
    private Set<String> featureIds;

    public PRTreeFeatureIndexer() {
        prTree = new PRTree<FeatureBounds>(this, 2);
        featureIds = new HashSet<>();
    }

    @Override
    public void addFeatures(final List<FeatureBounds> features) {
        prTree.load(features);

        for (FeatureBounds feature : features) {
            featureIds.add(feature.id);
        }
    }

    @Override
    public Collection<String> findFeatureIds(BoundingBox horizontalExtent,
            Extent<Double> verticalExtent, Extent<DateTime> timeExtent,
            Collection<String> variableIds) {

        Double zLow = -Double.MAX_VALUE;
        Double zHigh = Double.MAX_VALUE;
        if (verticalExtent != null) {
            zLow = verticalExtent.getLow();
            zHigh = verticalExtent.getHigh();
        }
        Long tLow = -Long.MAX_VALUE;
        Long tHigh = Long.MAX_VALUE;
        if (timeExtent != null) {
            tLow = timeExtent.getLow().getMillis();
            tHigh = timeExtent.getHigh().getMillis();
        }
        MBR mbr = new SimpleMBR(horizontalExtent.getMinX(), horizontalExtent.getMaxX(),
                horizontalExtent.getMinY(), horizontalExtent.getMaxY(), zLow, zHigh, tLow, tHigh);
        Iterable<FeatureBounds> features = prTree.find(mbr);
        Collection<String> featureIds = new ArrayList<>();
        for (FeatureBounds feature : features) {
            featureIds.add(feature.id);
        }
        return featureIds;
    }

    @Override
    public Set<String> getAllFeatureIds() {
        return featureIds;
    }

    @Override
    public int getDimensions() {
        return 4;
    }

    @Override
    public double getMax(int axis, FeatureBounds bounds) {
        switch (axis) {
        case 0:
            return bounds.horizontalPosition.getX();
        case 1:
            return bounds.horizontalPosition.getY();
        case 2:
            return bounds.verticalExtent.getHigh();
        case 3:
            return bounds.timeExtent.getHigh();
        default:
            return Double.NaN;
        }
    }

    @Override
    public double getMin(int axis, FeatureBounds bounds) {
        switch (axis) {
        case 0:
            return bounds.horizontalPosition.getX();
        case 1:
            return bounds.horizontalPosition.getY();
        case 2:
            return bounds.verticalExtent.getLow();
        case 3:
            return bounds.timeExtent.getLow();
        default:
            return Double.NaN;
        }
    }
}
