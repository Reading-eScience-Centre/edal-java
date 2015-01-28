/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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

package uk.ac.rdg.resc.edal.domain;

import java.util.Iterator;
import java.util.List;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.exceptions.IncorrectDomainException;
import uk.ac.rdg.resc.edal.exceptions.MismatchedCrsException;
import uk.ac.rdg.resc.edal.feature.TrajectoryFeature;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array1D;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.ImmutableArray1D;

/**
 * The domain of a {@link TrajectoryFeature}: a set of {@link GeoPosition}s
 * which are ordered in time.
 * 
 * @author Guy Griffiths
 * @author Jon Blower
 */
public class TrajectoryDomain implements DiscretePointDomain<GeoPosition> {

    private final Array1D<GeoPosition> positions;

    private final BoundingBox bbox;
    private final Extent<Double> zExtent;
    private final Extent<DateTime> tExtent;

    private final CoordinateReferenceSystem crs;
    private final VerticalCrs vCrs;
    private final Chronology chronology;

    public TrajectoryDomain(List<GeoPosition> positions) throws MismatchedCrsException, IncorrectDomainException {
        this.positions = new ImmutableArray1D<GeoPosition>(positions.toArray(new GeoPosition[0]));
        CoordinateReferenceSystem commonCrs = null;
        VerticalCrs commonVCrs = null;
        Chronology commonChronology = null;

        double minx = Double.MAX_VALUE;
        double maxx = -Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxy = -Double.MAX_VALUE;
        double minz = Double.MAX_VALUE;
        double maxz = -Double.MAX_VALUE;
        long lastTime = -Long.MAX_VALUE;
        
        for (GeoPosition position : positions) {
            HorizontalPosition hPos = position.getHorizontalPosition();
            VerticalPosition zPos = position.getVerticalPosition();
            DateTime time = position.getTime();
            if(time == null) {
                throw new IncorrectDomainException("All positions in a trajectory must have a time value");
            }
            
            if (commonCrs == null) {
                commonCrs = hPos.getCoordinateReferenceSystem();
            } else {
                if (!commonCrs.equals(hPos.getCoordinateReferenceSystem())) {
                    hPos = GISUtils.transformPosition(hPos, commonCrs);
                }
            }
            if (zPos != null) {
                if (commonVCrs == null) {
                    commonVCrs = zPos.getCoordinateReferenceSystem();
                } else {
                    if (!commonVCrs.equals(zPos.getCoordinateReferenceSystem())) {
                        throw new MismatchedCrsException(
                                "Vertical CRSs must match for all points in a Trajectory Domain");
                    }
                }
                if (!Double.isNaN(zPos.getZ())) {
                    minz = Math.min(minz, zPos.getZ());
                    maxz = Math.max(maxz, zPos.getZ());
                }
            }
            if (commonChronology == null) {
                commonChronology = time.getChronology();
            } else {
                if (!commonChronology.equals(time.getChronology())) {
                    throw new MismatchedCrsException(
                            "Chronologies must match for all points in a Trajectory Domain");
                }
            }
            if(time.getMillis() < lastTime) {
                throw new IncorrectDomainException("All points in a TrajectoryDomain must be in time order");
            }
            lastTime = time.getMillis();
            
            if (!Double.isNaN(hPos.getX())) {
                minx = Math.min(minx, hPos.getX());
                maxx = Math.max(maxx, hPos.getX());
            }
            if (!Double.isNaN(hPos.getY())) {
                miny = Math.min(miny, hPos.getY());
                maxy = Math.max(maxy, hPos.getY());
            }

        }
        crs = commonCrs;
        vCrs = commonVCrs;
        chronology = commonChronology;

        bbox = new BoundingBoxImpl(minx, miny, maxx, maxy, crs);
        if (minz != Double.MAX_VALUE) {
            zExtent = Extents.newExtent(minz, maxz);
        } else {
            /*
             * The minimum z-value hasn't changed, so we don't have a z-axis for
             * any of the positions in this domain
             */
            zExtent = Extents.emptyExtent(Double.class);
        }
        tExtent = Extents.newExtent(positions.get(0).getTime(), positions.get(positions.size() - 1).getTime());
    }

    /**
     * @return The vertical coordinate reference system used to reference the
     *         {@link #getDomainObjects() positions}.
     */
    public VerticalCrs getVerticalCrs() {
        return vCrs;
    }

    /**
     * @return The horizontal coordinate reference system used to reference the
     *         {@link #getDomainObjects() positions}.
     */
    public CoordinateReferenceSystem getHorizontalCrs() {
        return crs;
    }

    /**
     * @return The calendar system used to reference the temporal components of
     *         the {@link #getDomainObjects() positions}.
     */
    public Chronology getChronology() {
        return chronology;
    }

    /**
     * @return A {@link BoundingBox} which fully contains this
     *         {@link TrajectoryDomain}
     */
    public BoundingBox getCoordinateBounds() {
        return bbox;
    }

    /**
     * @return An {@link Extent} which fully contains this
     *         {@link TrajectoryDomain}
     */
    public Extent<DateTime> getTimeExtent() {
        return tExtent;
    }

    /**
     * @return An {@link Extent} which fully contains this
     *         {@link TrajectoryDomain}
     */
    public Extent<Double> getVerticalExtent() {
        return zExtent;
    }

    /**
     * @return The number of {@link GeoPosition} objects in this
     *         {@link TrajectoryDomain}
     */
    public int size() {
        return (int) positions.size();
    }

    @Override
    public Array1D<GeoPosition> getDomainObjects() {
        return positions;
    }

    @Override
    public boolean contains(GeoPosition position) {
        if (position == null) {
            return false;
        }
        Iterator<GeoPosition> iterator = positions.iterator();
        while (iterator.hasNext()) {
            if (position.equals(iterator.next())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((positions == null) ? 0 : positions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TrajectoryDomain other = (TrajectoryDomain) obj;
        if (positions == null) {
            if (other.positions != null)
                return false;
        } else if (!positions.equals(other.positions))
            return false;
        return true;
    }
}
