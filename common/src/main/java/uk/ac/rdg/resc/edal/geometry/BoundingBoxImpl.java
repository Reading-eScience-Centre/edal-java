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

package uk.ac.rdg.resc.edal.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.sis.referencing.CRS;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.internal.referencing.ReferencingUtilities;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.RangeMeaning;
import org.opengis.referencing.operation.TransformException;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Immutable implementation of a {@link BoundingBox}.
 *
 * @author Guy Griffiths
 * @author Jon
 */
public final class BoundingBoxImpl extends AbstractPolygon implements BoundingBox {
    private static final long serialVersionUID = 1L;

    private final double minx;
    private final double miny;
    private final double maxx;
    private final double maxy;
    private final CoordinateReferenceSystem crs;

    public static BoundingBoxImpl global() {
        return new BoundingBoxImpl(-180, -90, 180, 90, GISUtils.defaultGeographicCRS());
    }

    /**
     * Creates a {@link BoundingBox} from the minimum and maximum axis values,
     * with the default CRS
     */
    public BoundingBoxImpl(double minx, double miny, double maxx, double maxy) {
        this(minx, miny, maxx, maxy, GISUtils.defaultGeographicCRS());
    }

    /**
     * Creates a {@link BoundingBox} from the minimum and maximum axis values
     */
    public BoundingBoxImpl(double minx, double miny, double maxx, double maxy,
            CoordinateReferenceSystem crs) {
        this.minx = minx;
        this.maxx = maxx;
        this.miny = miny;
        this.maxy = maxy;
        /*
         * Check the bounds of the bbox
         */
        if (this.minx > this.maxx || this.miny > this.maxy) {
            throw new IllegalArgumentException("Invalid bounding box specification: " + minx + ","
                    + miny + ":" + maxx + "," + maxy);
        }
        this.crs = crs;
    }

    /**
     * Creates a {@link BoundingBox} from the x- and y- extents
     */
    public BoundingBoxImpl(Extent<Double> xExtent, Extent<Double> yExtent,
            CoordinateReferenceSystem crs) {
        this.minx = xExtent.getLow();
        this.maxx = xExtent.getHigh();
        this.miny = yExtent.getLow();
        this.maxy = yExtent.getHigh();
        this.crs = crs;
    }

    /**
     * Creates a {@link BoundingBox} from an existing
     * {@link GeographicBoundingBox}
     *
     * @param gbb
     *            The {@link GeographicBoundingBox} defining this
     *            {@link BoundingBox}
     */
    public BoundingBoxImpl(GeographicBoundingBox gbb) {
        this(gbb.getWestBoundLongitude(), gbb.getSouthBoundLatitude(), gbb.getEastBoundLongitude(),
                gbb.getNorthBoundLatitude(), GISUtils.defaultGeographicCRS());
    }

    /**
     * Creates a {@link BoundingBox} from a {@link CoordinateReferenceSystem},
     * where the bounds are the limits of validity of the CRS
     *
     * @param crs
     */
    public BoundingBoxImpl(CoordinateReferenceSystem crs) {
        //
        // TODO: replace the code below by the following block after Apache SIS 0.8 release:
        //
        // Envelope envelope = CRS.getDomainOfValidity(crs);
        //
        Envelope envelope = null;
        final GeographicBoundingBox bbox = CRS.getGeographicBoundingBox(crs);
        if (bbox != null && !Boolean.FALSE.equals(bbox.getInclusion())) {
            final SingleCRS targetCRS = CRS.getHorizontalComponent(crs);
            GeographicCRS sourceCRS = ReferencingUtilities.toNormalizedGeographicCRS(targetCRS);
            if (sourceCRS != null) {
                GeneralEnvelope bounds = new GeneralEnvelope(bbox);
                bounds.translate(-CRS.getGreenwichLongitude(sourceCRS), 0);
                bounds.setCoordinateReferenceSystem(sourceCRS);
                try {
                    envelope = Envelopes.transform(bounds, targetCRS);
                } catch (TransformException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        // End of TODO block
        if (envelope == null) {
            throw new IllegalArgumentException(
                    "The given CRS does not specify a domain of validity.");
        }
        this.minx = envelope.getMinimum(0);
        this.maxx = envelope.getMaximum(0);
        this.miny = envelope.getMinimum(1);
        this.maxy = envelope.getMaximum(1);
        this.crs = crs;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    @Override
    public double getMinX() {
        return minx;
    }

    @Override
    public double getMaxX() {
        return maxx;
    }

    @Override
    public double getMinY() {
        return miny;
    }

    @Override
    public double getMaxY() {
        return maxy;
    }

    @Override
    public double getWidth() {
        return maxx - minx;
    }

    @Override
    public double getHeight() {
        return maxy - miny;
    }

    @Override
    public HorizontalPosition getLowerCorner() {
        return new HorizontalPosition(minx, miny, crs);
    }

    @Override
    public HorizontalPosition getUpperCorner() {
        return new HorizontalPosition(maxx, maxy, crs);
    }

    @Override
    public String toString() {
        return String.format("%f, %f - %f, %f", minx, miny, maxx, maxy);
    }

    /**
     * Returns a list of vertices in anticlockwise order starting at minx, miny
     */
    @Override
    public List<HorizontalPosition> getVertices() {
        List<HorizontalPosition> positions = new ArrayList<HorizontalPosition>();
        positions.add(new HorizontalPosition(minx, miny, crs));
        positions.add(new HorizontalPosition(maxx, miny, crs));
        positions.add(new HorizontalPosition(maxx, maxy, crs));
        positions.add(new HorizontalPosition(minx, maxy, crs));
        return Collections.unmodifiableList(positions);
    }

    /**
     * Provides a more efficient contains() method than the one in
     * AbstractPolygon
     */
    @Override
    public boolean contains(double x, double y) {
        CoordinateSystem coordinateSystem = crs.getCoordinateSystem();
        if (coordinateSystem.getDimension() >= 2) {
            if (coordinateSystem.getAxis(0).getRangeMeaning() == RangeMeaning.WRAPAROUND) {
                x = GISUtils.getNextEquivalentLongitude(minx, x);
            }
            if (coordinateSystem.getAxis(1).getRangeMeaning() == RangeMeaning.WRAPAROUND) {
                y = GISUtils.getNextEquivalentLongitude(miny, y);
            }
        }
        return (x >= minx && x <= maxx && y >= miny && y <= maxy);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((crs == null) ? 0 : crs.hashCode());
        long temp;
        temp = Double.doubleToLongBits(maxx);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxy);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minx);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(miny);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        BoundingBoxImpl other = (BoundingBoxImpl) obj;
        if (crs == null) {
            if (other.crs != null)
                return false;
        } else if (!crs.toString().equals(other.crs.toString()))
            return false;
        if (Double.doubleToLongBits(maxx) != Double.doubleToLongBits(other.maxx))
            return false;
        if (Double.doubleToLongBits(maxy) != Double.doubleToLongBits(other.maxy))
            return false;
        if (Double.doubleToLongBits(minx) != Double.doubleToLongBits(other.minx))
            return false;
        if (Double.doubleToLongBits(miny) != Double.doubleToLongBits(other.miny))
            return false;
        return true;
    }
}
