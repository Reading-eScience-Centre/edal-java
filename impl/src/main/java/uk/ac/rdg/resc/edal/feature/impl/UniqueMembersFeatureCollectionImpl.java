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

import java.util.List;

import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.UniqueMembersFeatureCollection;

public class UniqueMembersFeatureCollectionImpl<F extends Feature> extends FeatureCollectionImpl<F> implements UniqueMembersFeatureCollection<F> {

    public UniqueMembersFeatureCollectionImpl(String collectionId, String collectionName) {
        super(collectionId, collectionName);
    }
    
    @Override
    protected void addFeature(F feature) {
        for (String memberName : feature.getCoverage().getScalarMemberNames()) {
            List<F> featuresWithMember = getFeaturesWithMember(memberName);
            if (featuresWithMember != null && featuresWithMember.size() > 0) {
                throw new IllegalArgumentException(
                        "Cannot add this feature - we already have a feature with the member name: "
                                + memberName);
            }
        }
        super.addFeature(feature);
    };

    @Override
    public F getFeatureContainingMember(String memberName) {
        List<F> featuresWithMember = getFeaturesWithMember(memberName);
        if(featuresWithMember.size() == 0){
            return null;
        } else {
            /*
             * There is a unique constraint, and it is applied which adding the
             * feature, so we just use an assertion here. If it's failing,
             * something unusual is happening, and the code needs to be looked
             * at
             */
            assert (featuresWithMember.size() == 1);
            return featuresWithMember.get(0);
        }
    }
  
}
