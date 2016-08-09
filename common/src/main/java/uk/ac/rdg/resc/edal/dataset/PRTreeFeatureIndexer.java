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
import org.opengis.metadata.extent.GeographicBoundingBox;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * This uses a {@link PRTree} to index features spatially.
 *
 * All features within this {@link PRTree} have their positions specified in
 * WGS84 with longitudes in the range (-180:180] - this is guaranteed by the
 * {@link PRTreeFeatureIndexer#addFeatures} method.
 *
 * @author Guy Griffiths
 */
public class PRTreeFeatureIndexer implements FeatureIndexer,
        MBRConverter<FeatureIndexer.FeatureBounds> {

    private static final long serialVersionUID = 1L;
    private PRTree<FeatureBounds> prTree;
    private Set<String> featureIds;

    public PRTreeFeatureIndexer() {
        prTree = new PRTree<FeatureBounds>(this, 2);
        featureIds = new HashSet<>();
    }

    @Override
    public void addFeatures(final List<FeatureBounds> features) {
        for (FeatureBounds feature : features) {
            featureIds.add(feature.id);

            /*
             * Transform to WGS84 if required
             */
            if (!GISUtils.isWgs84LonLat(feature.horizontalPosition.getCoordinateReferenceSystem())) {
                feature.horizontalPosition = GISUtils.transformPosition(feature.horizontalPosition,
                        GISUtils.defaultGeographicCRS());
            }

            /*
             * Now ensure position is in the range (-180:180]
             */
            double constrainedX = GISUtils.constrainLongitude180(feature.horizontalPosition.getX());
            if (feature.horizontalPosition.getX() != constrainedX) {
                feature.horizontalPosition = new HorizontalPosition(constrainedX,
                        feature.horizontalPosition.getY(),
                        feature.horizontalPosition.getCoordinateReferenceSystem());
            }
        }

        prTree.load(features);
    }

    @Override
    public Collection<String> findFeatureIds(BoundingBox horizontalExtent,
            Extent<Double> verticalExtent, Extent<DateTime> timeExtent,
            Collection<String> variableIds) {

        if (!GISUtils.isWgs84LonLat(horizontalExtent.getCoordinateReferenceSystem())) {
            GeographicBoundingBox geographicBoundingBox = GISUtils
                    .toGeographicBoundingBox(horizontalExtent);
            horizontalExtent = new BoundingBoxImpl(geographicBoundingBox.getWestBoundLongitude(),
                    geographicBoundingBox.getSouthBoundLatitude(),
                    geographicBoundingBox.getEastBoundLongitude(),
                    geographicBoundingBox.getNorthBoundLatitude(), GISUtils.defaultGeographicCRS());
        }

        /*
         * Transform bounding box to lat-lon here with min value in range
         * (-180:180].
         *
         * We do this manually rather than using GISUtils.constrainLongitude180
         * because we want to shift both sides of the bounding box by the same
         * amount.
         */
        double minx = horizontalExtent.getMinX();
        double maxx = horizontalExtent.getMaxX();
        boolean changed = false;
        while (minx > 180) {
            minx -= 360.0;
            maxx -= 360.0;
            changed = true;
        }
        while (minx <= -180) {
            minx += 360.0;
            maxx += 360.0;
            changed = true;
        }
        if (changed) {
            horizontalExtent = new BoundingBoxImpl(minx, horizontalExtent.getMinY(), maxx,
                    horizontalExtent.getMaxY(), horizontalExtent.getCoordinateReferenceSystem());
        }

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

        /*
         * Check to see if we have a bounding box which crosses the date line.
         * If so, make 2 requests to the PRTree
         */
        Collection<String> featureIds = new ArrayList<>();
        Iterable<FeatureBounds> features;
        if (horizontalExtent.getMaxX() > 180) {
            if (horizontalExtent.getMaxX() > 540) {
                /*
                 * We have a bounding box that spans the date line at both ends
                 */
                MBR mbr = new SimpleMBR(-180, 180, horizontalExtent.getMinY(),
                        horizontalExtent.getMaxY(), zLow, zHigh, tLow, tHigh);
                features = prTree.find(mbr);
            } else {
                MBR mbr = new SimpleMBR(horizontalExtent.getMinX(), 180,
                        horizontalExtent.getMinY(), horizontalExtent.getMaxY(), zLow, zHigh, tLow,
                        tHigh);
                features = prTree.find(mbr);
                for (FeatureBounds feature : features) {
                    featureIds.add(feature.id);
                }

                mbr = new SimpleMBR(-180,
                        GISUtils.constrainLongitude180(horizontalExtent.getMaxX()),
                        horizontalExtent.getMinY(), horizontalExtent.getMaxY(), zLow, zHigh, tLow,
                        tHigh);
                features = prTree.find(mbr);
            }
            for (FeatureBounds feature : features) {
                featureIds.add(feature.id);
            }
        } else {
            MBR mbr = new SimpleMBR(horizontalExtent.getMinX(), horizontalExtent.getMaxX(),
                    horizontalExtent.getMinY(), horizontalExtent.getMaxY(), zLow, zHigh, tLow,
                    tHigh);
            features = prTree.find(mbr);
            for (FeatureBounds feature : features) {
                /*
                 * Make sure that the returned features contain all of the
                 * required variables.
                 */
                boolean addFeature = true;
                for (String varId : variableIds) {
                    if (!feature.variableIds.contains(varId)) {
                        addFeature = false;
                        break;
                    }
                }
                if (addFeature) {
                    featureIds.add(feature.id);
                }
            }
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
