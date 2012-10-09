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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.MetadataUtils;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;

public class FeatureCollectionImpl<R extends Feature> implements FeatureCollection<R> {
    private String collectionId;
    private String name;
    private Map<String, R> id2Feature;
    private Map<String, Collection<R>> member2Features;

    public FeatureCollectionImpl(String collectionId, String collectionName) {
        this.collectionId = collectionId;
        this.name = collectionName;

        id2Feature = new LinkedHashMap<String, R>();
        member2Features = new LinkedHashMap<String, Collection<R>>();
    }

    @Override
    public R getFeatureById(String id) {
        return id2Feature.get(id);
    }

    @Override
    public Set<String> getFeatureIds() {
        return id2Feature.keySet();
    }

    @Override
    public Collection<R> getFeatures() {
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

    @Override
    public Iterator<R> iterator() {
        return id2Feature.values().iterator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Class<R> getFeatureType() {
        return (Class<R>) Feature.class;
    }
    
    protected void addFeature(R feature){
        id2Feature.put(feature.getId(), feature);
        List<RangeMetadata> allMetadata = MetadataUtils.getAllTreeMembers(feature.getCoverage().getRangeMetadata());
        for(RangeMetadata metadata : allMetadata){
            String memberName = metadata.getName();
            Collection<R> features = member2Features.get(memberName);
            if(features == null){
                features = new ArrayList<R>();
            }
            features.add(feature);
            member2Features.put(memberName, features);
        }
    }
    
    @Override
    public Set<String> getMemberIdsInCollection() {
        return member2Features.keySet();
    }
    
    @Override
    public Collection<R> getFeaturesWithMember(String memberName) {
        return member2Features.get(memberName);
    }
}
