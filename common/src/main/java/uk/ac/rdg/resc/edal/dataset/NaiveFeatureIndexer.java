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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

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
public class NaiveFeatureIndexer implements FeatureIndexer {

    private class FeatureBounds {
        List<HorizontalPosition> horizontalPositions;
        Extent<Double> verticalExtent;
        Extent<DateTime> timeExtent;
        Collection<String> variableIds;

        public FeatureBounds(List<HorizontalPosition> horizontalPositions,
                Extent<Double> verticalExtent, Extent<DateTime> timeExtent,
                Collection<String> variableIds) {
            super();
            this.horizontalPositions = horizontalPositions;
            this.verticalExtent = verticalExtent;
            this.timeExtent = timeExtent;
            this.variableIds = variableIds;
        }
    }

    private Map<String, FeatureBounds> features = new HashMap<String, NaiveFeatureIndexer.FeatureBounds>();

    public void addFeature(String featureId, List<HorizontalPosition> horizontalPositions,
            Extent<Double> verticalExtent, Extent<DateTime> timeExtent,
            Collection<String> variableIds) {
        features.put(featureId, new FeatureBounds(horizontalPositions, verticalExtent, timeExtent,
                variableIds));
    }

    @Override
    public Collection<String> findFeatureIds(BoundingBox horizontalExtent,
            Extent<Double> verticalExtent, Extent<DateTime> timeExtent,
            Collection<String> variableIds) {
        List<String> featureIds = new ArrayList<String>();
        int hRej = 0;
        int zRej = 0;
        int tRej = 0;
        int vRej = 0;
        for (Entry<String, FeatureBounds> entry : features.entrySet()) {
            FeatureBounds bounds = entry.getValue();
            boolean hContains = false;
            for (HorizontalPosition pos : bounds.horizontalPositions) {
                if (horizontalExtent == null || horizontalExtent.contains(pos)) {
                    hContains = true;
                    break;
                }
            }
            if (!hContains) {
                hRej++;
                continue;
            }
            if (verticalExtent != null && !verticalExtent.intersects(bounds.verticalExtent)) {
                zRej++;
                continue;
            }
            if (timeExtent != null && !timeExtent.intersects(bounds.timeExtent)) {
                tRej++;
                continue;
            }
            boolean varsContain = false;
            if (variableIds != null) {
                for (String varId : variableIds) {
                    if (bounds.variableIds.contains(varId)) {
                        varsContain = true;
                        break;
                    }
                }
            } else {
                varsContain = true;
            }
            if (varsContain) {
                featureIds.add(entry.getKey());
            } else {
                vRej++;
            }
        }
        //        System.out.println(featureIds.size()+" found from indexing");
        //        System.out.println(hRej+" features rejected horizontally");
        //        System.out.println(zRej+" features rejected vertically");
        //        System.out.println(tRej+" features rejected temporally");
        //        System.out.println(vRej+" features rejected on variables");
        return featureIds;
    }

    @Override
    public Set<String> getAllFeatureIds() {
        return features.keySet();
    }
}
