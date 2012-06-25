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

import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * An implementation of a {@link PointSeriesFeature}
 * 
 * @author Guy Griffiths
 */
public class PointSeriesFeatureImpl extends AbstractFeature implements PointSeriesFeature {

    private final PointSeriesCoverage coverage;
    private final HorizontalPosition hPos;
    private final VerticalPosition vPos;

    public PointSeriesFeatureImpl(String name, String id, String description,
            PointSeriesCoverage coverage, HorizontalPosition hPos, VerticalPosition vPos,
            FeatureCollection<? extends Feature> parentCollection) {
        super(name, id, description, parentCollection);
        this.coverage = coverage;
        this.hPos = hPos;
        this.vPos = vPos;
    }

    @Override
    public PointSeriesCoverage getCoverage() {
        return coverage;
    }

    @Override
    public HorizontalPosition getHorizontalPosition() {
        return hPos;
    }

    @Override
    public VerticalPosition getVerticalPosition() {
        return vPos;
    }
}
