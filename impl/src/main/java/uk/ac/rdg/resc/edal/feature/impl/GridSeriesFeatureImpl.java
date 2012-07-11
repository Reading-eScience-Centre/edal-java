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

import java.util.Set;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.edal.feature.FeatureCollection;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.feature.GridSeriesFeature;
import uk.ac.rdg.resc.edal.feature.PointSeriesFeature;
import uk.ac.rdg.resc.edal.feature.ProfileFeature;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.position.impl.VerticalPositionImpl;

/**
 * An implementation of a {@link GridSeriesFeature}
 * 
 * @author Guy Griffiths
 */
public class GridSeriesFeatureImpl extends AbstractFeature implements GridSeriesFeature {

    private final GridSeriesCoverage coverage;

    public GridSeriesFeatureImpl(String name, String id,
            FeatureCollection<? extends Feature> parentCollection, GridSeriesCoverage coverage) {
        super(name, id, coverage.getDescription(), parentCollection);
        this.coverage = coverage;
    }
    
    @Override
    public GridSeriesCoverage getCoverage() {
        return coverage;
    }

    @Override
    public PointSeriesFeature extractPointSeriesFeature(HorizontalPosition pos, VerticalPosition z,
            Extent<TimePosition> tRange, Set<String> members) {
        PointSeriesCoverage psCoverage = coverage.extractPointSeriesCoverage(pos, z, tRange,
                members == null ? coverage.getScalarMemberNames() : members);
        GridSeriesDomain domain = coverage.getDomain();
        HorizontalPosition centre = domain.getHorizontalGrid().findContainingCell(pos).getCentre();
        int zIndex = domain.getVerticalAxis().findIndexOf(z.getZ());
        VerticalPosition vCentre = new VerticalPositionImpl(domain.getVerticalAxis()
                .getCoordinateValue(zIndex), domain.getVerticalCrs());
        return new PointSeriesFeatureImpl(getName() + " -> PointSeries", "PS-" + getId(),
                "Point series extraction of " + getDescription(), psCoverage, centre, vCentre,
                getFeatureCollection());
    }

    @Override
    public ProfileFeature extractProfileFeature(HorizontalPosition pos, TimePosition t,
            Set<String> members) {
        ProfileCoverage pCoverage = coverage.extractProfileCoverage(pos, t,
                members == null ? coverage.getScalarMemberNames() : members);
        GridSeriesDomain domain = coverage.getDomain();
        HorizontalPosition centre = domain.getHorizontalGrid().findContainingCell(pos).getCentre();
        int tIndex = domain.getTimeAxis().findIndexOf(t);
        TimePosition tCentre = domain.getTimeAxis().getCoordinateValue(tIndex);
        return new ProfileFeatureImpl(getName() + " -> Profile", "PF-" + getId(),
                "Profile extraction of " + getDescription(), pCoverage, centre, tCentre,
                getFeatureCollection());
    }

    @Override
    public GridFeature extractGridFeature(HorizontalGrid targetDomain, VerticalPosition zPos,
            TimePosition tPos, Set<String> members) {
        GridCoverage2D gridCoverage = coverage.extractGridCoverage(targetDomain, zPos, tPos,members);
        return new GridFeatureImpl(getName() + " -> GridFeature", "GF-" + getId(),
                getFeatureCollection(), gridCoverage);
    }
}
