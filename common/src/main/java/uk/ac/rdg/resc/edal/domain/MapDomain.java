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

import org.joda.time.DateTime;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.RegularGrid;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

import java.io.Serializable;

/**
 * Implementation of a {@link MapDomain}
 * 
 * @author Guy
 */
public class MapDomain implements RectilinearGrid, Serializable {
    private static final long serialVersionUID = 1L;

    private RectilinearGrid hGrid;
    private Double z;
    private VerticalCrs vCrs;
    private DateTime time;

    /**
     * Instantiates a {@link MapDomain} based on a {@link RegularGrid} and z/t
     * values, with no specified vertical CRS.
     * 
     * @param bbox
     *            The {@link BoundingBox} of the {@link MapDomain}
     * @param width
     *            The desired width of the {@link MapDomain}
     * @param height
     *            The desired height of the {@link MapDomain}
     * @param z
     *            The vertical value of this {@link MapDomain}
     * @param time
     *            The time value of this {@link MapDomain}
     */
    public MapDomain(BoundingBox bbox, int width, int height, Double z, DateTime time) {
        this.hGrid = new RegularGridImpl(bbox, width, height);
        this.z = z;
        this.time = time;
        this.vCrs = null;
    }

    /**
     * Instantiates a {@link MapDomain} from a {@link RectilinearGrid} and z/t
     * values, with no specified vertical CRS. Used in cases where
     * {@link VerticalCrs} is not important
     * 
     * @param hGrid
     *            The {@link RectilinearGrid} forming the {@link MapDomain}
     * @param z
     *            The vertical value of this {@link MapDomain}
     * @param time
     *            The time value of this {@link MapDomain}
     */
    public MapDomain(RectilinearGrid hGrid, Double z, DateTime time) {
        this.hGrid = hGrid;
        this.z = z;
        this.vCrs = null;
        this.time = time;
    }

    /**
     * Instantiates a {@link MapDomain} from a {@link RectilinearGrid} and z/t
     * values
     * 
     * @param hGrid
     *            The {@link RectilinearGrid} forming the {@link MapDomain}
     * @param z
     *            The vertical value of this {@link MapDomain}
     * @param vCrs
     *            The {@link VerticalCrs} under which to interpret
     *            <code>z</code>
     * @param time
     *            The time value of this {@link MapDomain}
     */
    public MapDomain(RectilinearGrid hGrid, Double z, VerticalCrs vCrs, DateTime time) {
        this.hGrid = hGrid;
        this.z = z;
        this.vCrs = vCrs;
        this.time = time;
    }

    public Double getZ() {
        return z;
    }

    public VerticalCrs getVerticalCrs() {
        return vCrs;
    }

    public void setVerticalCrs(VerticalCrs vCrs) {
        this.vCrs = vCrs;
    }

    public DateTime getTime() {
        return time;
    }

    @Override
    public long size() {
        return hGrid.size();
    }

    @Override
    public int getXSize() {
        return hGrid.getXSize();
    }

    @Override
    public int getYSize() {
        return hGrid.getYSize();
    }

    @Override
    public ReferenceableAxis<Double> getXAxis() {
        return hGrid.getXAxis();
    }

    @Override
    public ReferenceableAxis<Double> getYAxis() {
        return hGrid.getYAxis();
    }

    @Override
    public GridCoordinates2D findIndexOf(HorizontalPosition position) {
        return hGrid.findIndexOf(position);
    }

    @Override
    public Array2D<GridCell2D> getDomainObjects() {
        return hGrid.getDomainObjects();
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return hGrid.contains(position);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return hGrid.getBoundingBox();
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() {
        return hGrid.getGeographicBoundingBox();
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return hGrid.getCoordinateReferenceSystem();
    }
    
    public RectilinearGrid getHorizontalGrid() {
        return hGrid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hGrid == null) ? 0 : hGrid.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        result = prime * result + ((vCrs == null) ? 0 : vCrs.hashCode());
        result = prime * result + ((z == null) ? 0 : z.hashCode());
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
        MapDomain other = (MapDomain) obj;
        if (hGrid == null) {
            if (other.hGrid != null)
                return false;
        } else if (!hGrid.equals(other.hGrid))
            return false;
        if (time == null) {
            if (other.time != null)
                return false;
        } else if (!time.equals(other.time))
            return false;
        if (vCrs == null) {
            if (other.vCrs != null)
                return false;
        } else if (!vCrs.equals(other.vCrs))
            return false;
        if (z == null) {
            if (other.z != null)
                return false;
        } else if (!z.equals(other.z))
            return false;
        return true;
    }
}
