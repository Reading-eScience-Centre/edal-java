/*******************************************************************************
 * Copyright (c) 2018 The University of Reading
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

package uk.ac.rdg.resc.edal.dataset.cdm;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.ProjectionPoint;
import ucar.unidata.geoloc.ProjectionPointImpl;
import uk.ac.rdg.resc.edal.grid.cdm.CdmTransformedGrid;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * A {@link Projection} which defines a rotated and offset grid in an arbitrary
 * CRS. This does not support all of the methods of {@link Projection}, but
 * rather only those which are required by {@link CdmTransformedGrid}.
 * 
 * To clarify, coordinates in this projection need to be rotated clockwise by an
 * angle and then shifted by an offset before they are then in the correct
 * geographic position in the supplied CRS. They therefore need to be
 * transformed from that CRS to CRS:84 to give a lat-lon position.
 * 
 * 
 * 
 * @author Guy Griffiths
 */
public class RotatedOffsetProjection extends ProjectionImpl {
    private static final long serialVersionUID = 1L;
    private final double originX;
    private final double originY;
    private final double angle;
    private final CoordinateReferenceSystem crs;

    /*
     * Cached sin/cos of angle/negative angle
     */
    private final double sa;
    private final double ca;
    private final double sna;
    private final double cna;

    /**
     * 
     * @param originX
     *            The origin in the x-dimension, in the supplied CRS, at which
     *            this rotated grid starts
     * @param originY
     *            The origin in the y-dimension, in the supplied CRS, at which
     *            this rotated grid starts
     * @param angle
     *            The CLOCKWISE angle by which the grid is rotated
     * @param crs
     *            The {@link CoordinateReferenceSystem} of the grid
     */
    public RotatedOffsetProjection(double originX, double originY, double angle,
            CoordinateReferenceSystem crs) {
        super("RotatedOffset", false);
        this.originX = originX;
        this.originY = originY;
        this.angle = GISUtils.DEG2RAD * angle;
        this.crs = crs;

        this.sa = Math.sin(angle);
        this.ca = Math.cos(angle);
        this.sna = Math.sin(-angle);
        this.cna = Math.cos(-angle);
    }

    @Override
    public ProjectionPoint latLonToProj(LatLonPoint latlon, ProjectionPointImpl destPoint) {
        double longitude = latlon.getLongitude();
        double latitude = latlon.getLatitude();

        HorizontalPosition pos = GISUtils
                .transformPosition(new HorizontalPosition(longitude, latitude), crs);
        double xRot = pos.getX() - originX;
        double yRot = pos.getY() - originY;

        double x = xRot * ca + yRot * sa;
        double y = -xRot * sa + yRot * ca;

        if (destPoint == null) {
            destPoint = new ProjectionPointImpl(x, y);
        } else {
            destPoint.setLocation(x, y);
        }
        return destPoint;
    }

    @Override
    public LatLonPoint projToLatLon(ProjectionPoint ppt, LatLonPointImpl destPoint) {
        double x = ppt.getX();
        double y = ppt.getY();

        double xRot = x * cna + y * sna;
        double yRot = -x * sna + y * cna;

        HorizontalPosition pos = GISUtils.transformPosition(
                new HorizontalPosition(xRot + originX, yRot + originY, crs),
                GISUtils.defaultGeographicCRS());

        if (destPoint == null) {
            destPoint = new LatLonPointImpl(pos.getY(), pos.getX());
        } else {
            destPoint.set(pos.getY(), pos.getX());
        }
        return destPoint;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(angle);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((crs == null) ? 0 : crs.hashCode());
        temp = Double.doubleToLongBits(originX);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(originY);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        RotatedOffsetProjection other = (RotatedOffsetProjection) obj;
        if (Double.doubleToLongBits(angle) != Double.doubleToLongBits(other.angle))
            return false;
        if (crs == null) {
            if (other.crs != null)
                return false;
        } else if (!crs.equals(other.crs))
            return false;
        if (Double.doubleToLongBits(originX) != Double.doubleToLongBits(other.originX))
            return false;
        if (Double.doubleToLongBits(originY) != Double.doubleToLongBits(other.originY))
            return false;
        return true;
    }

    /*
     * We don't use these other methods in CdmTransformedGrid, which is the
     * purpose of this projection
     */

    @Override
    public ProjectionImpl constructCopy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String paramsToString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean crossSeam(ProjectionPoint pt1, ProjectionPoint pt2) {
        throw new UnsupportedOperationException();
    }
}
