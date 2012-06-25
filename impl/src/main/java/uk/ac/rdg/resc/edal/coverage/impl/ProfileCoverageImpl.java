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

import java.util.HashMap;
import java.util.Map;

import uk.ac.rdg.resc.edal.Phenomenon;
import uk.ac.rdg.resc.edal.Unit;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.ProfileDomain;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.BigList;

/**
 * A mutable (only adding new members is supported) in-memory implementation of
 * {@link ProfileCoverage}
 * 
 * @author Guy Griffiths
 * 
 */
public class ProfileCoverageImpl extends
        AbstractMultimemberDiscreteCoverage<VerticalPosition, VerticalPosition, ProfileDomain>
        implements ProfileCoverage {

    private Map<String, BigList<?>> memberName2Values;

    public ProfileCoverageImpl(String description, ProfileDomain domain) {
        super(description, domain);
        memberName2Values = new HashMap<String, BigList<?>>();
    }

    public void addMember(String memberName, ProfileDomain domain, String description,
            Phenomenon parameter, Unit units, BigList<?> values) {
        addMemberToMetadata(memberName, domain, description, parameter, units);
        memberName2Values.put(memberName, values);
    }

    @Override
    public BigList<?> getValuesList(final String memberName) {
        if (!memberName2Values.containsKey(memberName)) {
            throw new IllegalArgumentException(memberName + " is not contained in this coverage");
        }
        return memberName2Values.get(memberName);
    }
}
