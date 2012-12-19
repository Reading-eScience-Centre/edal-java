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

package uk.ac.rdg.resc.edal.feature.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.MetadataUtils;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;

public class FeatureCollectionImpl<F extends Feature> implements FeatureCollection<F> {
    private String collectionId;
    private String name;
    private Map<String, F> id2Feature;
    private Map<String, List<F>> member2Features;
    
    private BoundingBox bbox; 
    private Extent<TimePosition> tExtent; 
    private Extent<VerticalPosition> zExtent; 

    public FeatureCollectionImpl(String collectionId, String collectionName) {
        this.collectionId = collectionId;
        this.name = collectionName;

        id2Feature = new LinkedHashMap<String, F>();
        member2Features = new LinkedHashMap<String, List<F>>();
        
        bbox = null;
        tExtent = Extents.emptyExtent(TimePosition.class);
        zExtent = Extents.emptyExtent(VerticalPosition.class);
    }

    @Override
    public F getFeatureById(String id) {
        return id2Feature.get(id);
    }

    @Override
    public Collection<F> getFeatures() {
        return id2Feature.values();
    }

    @Override
    public String getId() {
        return collectionId;
    }

    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<F> getFeatureType() {
        return (Class<F>) Feature.class;
    }
    
    protected void addFeature(F feature){
        id2Feature.put(feature.getId(), feature);
        List<RangeMetadata> allMetadata = MetadataUtils.getAllTreeMembers(feature.getCoverage().getRangeMetadata());
        for(RangeMetadata metadata : allMetadata){
            String memberName = metadata.getName();
            List<F> features = member2Features.get(memberName);
            if(features == null){
                features = new ArrayList<F>();
            }
            features.add(feature);
            member2Features.put(memberName, features);
        }
        
        possiblyExtendBounds(feature);
    }
    
    private void possiblyExtendBounds(F feature){
        BoundingBox fBbox = GISUtils.getFeatureHorizontalExtent(feature);
        boolean changed = false;
        if(bbox == null){
            bbox = fBbox;
        } else if(fBbox != null){
            double minx = bbox.getMinX();
            double maxx = bbox.getMaxX();
            double miny = bbox.getMinY();
            double maxy = bbox.getMaxY();
            if(fBbox.getMinX() < minx){
                minx = fBbox.getMinX();
                changed = true;
            }
            if(fBbox.getMaxX() > maxx){
                maxx = fBbox.getMaxX();
                changed = true;
            }
            if(fBbox.getMinY() < miny){
                miny = fBbox.getMinY();
                changed = true;
            }
            if(fBbox.getMaxY() > maxy){
                maxy = fBbox.getMaxY();
                changed = true;
            }
            if(changed){
                bbox = new BoundingBoxImpl(minx, miny, maxx, maxy, DefaultGeographicCRS.WGS84);
            }
        }
        
        Extent<TimePosition> fTExtent = GISUtils.getFeatureTimeExtent(feature);
        if(fTExtent != null){
            TimePosition lowT = tExtent.getLow();
            TimePosition highT = tExtent.getHigh();
            changed = false;
            if(lowT == null || fTExtent.getLow().compareTo(lowT) < 0){
                lowT = fTExtent.getLow();
                changed = true;
            }
            if(highT == null || fTExtent.getHigh().compareTo(highT) > 0){
                highT = fTExtent.getHigh();
                changed = true;
            }
            if(changed){
                tExtent = Extents.newExtent(lowT, highT);
            }
        }
        
        Extent<VerticalPosition> fZExtent = GISUtils.getFeatureVerticalExtent(feature);
        if(fZExtent != null){
            VerticalPosition lowZ = zExtent.getLow();
            VerticalPosition highZ = zExtent.getHigh();
            changed = false;
            if(lowZ == null || fZExtent.getLow().compareTo(lowZ) < 0){
                lowZ = fZExtent.getLow();
                changed = true;
            }
            if(highZ == null || fZExtent.getHigh().compareTo(highZ) > 0){
                highZ = fZExtent.getHigh();
                changed = true;
            }
            if(changed){
                zExtent = Extents.newExtent(lowZ, highZ);
            }
        }
    }
    
    @Override
    public Set<String> getMemberIdsInCollection() {
        return member2Features.keySet();
    }
    
    protected List<F> getFeaturesWithMember(String memberName) {
        return member2Features.get(memberName);
    }

    @Override
    public Collection<F> findFeatures(BoundingBox bbox, Extent<Double> zRange,
            Extent<TimePosition> tRange, Set<String> memberNames) {
        Set<F> includedFeatures = new HashSet<F>();
        
        List<F> features = new ArrayList<F>();
        if(memberNames == null){
            features.addAll(id2Feature.values());
        } else {
            for (String memberName : memberNames) {
                features.addAll(getFeaturesWithMember(memberName));
            }
        }
        if(features != null){
            for (F feature : features) {
                if (bbox != null && !GISUtils.featureOverlapsBoundingBox(bbox, feature)) {
                    continue;
                }
                if (tRange != null && !GISUtils.timeRangeContains(tRange, feature)) {
                    continue;
                }
                if (zRange != null && !GISUtils.zRangeContains(zRange, feature)) {
                    continue;
                }
                includedFeatures.add(feature);
            }
        }
        return includedFeatures;
    }

    @Override
    public BoundingBox getCollectionBoundingBox() {
        return bbox;
    }

    @Override
    public Extent<VerticalPosition> getCollectionVerticalExtent() {
        return zExtent;
    }

    @Override
    public Extent<TimePosition> getCollectionTimeExtent() {
        return tExtent;
    }
}
