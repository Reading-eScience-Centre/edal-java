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

package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.PointSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.PointSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.util.BigList;
import uk.ac.rdg.resc.edal.util.LittleBigList;

/**
 * A mutable (only adding new members is supported) in-memory implementation of
 * {@link PointSeriesCoverage}
 * 
 * @author Guy Griffiths
 * 
 */
public class PointSeriesCoverageImpl extends
        AbstractBigListBackedCoverage<TimePosition, TimePosition, PointSeriesDomain> implements
        PointSeriesCoverage {

    public PointSeriesCoverageImpl(String description, PointSeriesDomain domain) {
        super(description, domain);
    }

    @Override
    public PointSeriesCoverage extractSubCoverage(Extent<TimePosition> tExtent,
            Set<String> memberNames) {
        List<TimePosition> times = new ArrayList<TimePosition>();
        for (TimePosition time : getDomain().getTimes()) {
            if (time.compareTo(tExtent.getLow()) >= 0 && time.compareTo(tExtent.getHigh()) <= 0) {
                times.add(time);
            }
        }
        PointSeriesDomain domain = new PointSeriesDomainImpl(times);
        PointSeriesCoverageImpl subCoverage = new PointSeriesCoverageImpl(getDescription(), domain);

        long fromIndex = getDomain().findIndexOf(times.get(0));
        long toIndex = getDomain().findIndexOf(times.get(times.size() - 1)) + 1;
        if(memberNames == null){
            memberNames = getScalarMemberNames();
        }
        for (String memberName : memberNames) {
            BigList<?> allValues = getValues(memberName);
            LittleBigList<Object> requiredValues = new LittleBigList<Object>();
            requiredValues.addAll(allValues.getAll(fromIndex, toIndex));
            ScalarMetadata scalarMetadata = getScalarMetadata(memberName);
            subCoverage.addMember(memberName, domain, scalarMetadata.getDescription(),
                    scalarMetadata.getParameter(), scalarMetadata.getUnits(), requiredValues,
                    scalarMetadata.getValueType());
        }
        return subCoverage;
    }
}
