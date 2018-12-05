/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
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
 *******************************************************************************/
package uk.ac.rdg.resc.edal.grid.cdm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.ProjectionPoint;
import ucar.unidata.geoloc.projection.RotatedPole;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.geometry.SimplePolygon;
import uk.ac.rdg.resc.edal.grid.AbstractTransformedGrid;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.GridCell2DImpl;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array2D;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;
import uk.ac.rdg.resc.edal.util.cdm.CdmUtils;

/**
 * A two-dimensional {@link HorizontalGrid} that uses a {@link Projection} to
 * convert from lat-lon coordinates to grid coordinates.
 *
 * @author Jon Blower
 * @author Guy Griffiths
 */
public class CdmTransformedGrid extends AbstractTransformedGrid {
    private static final long serialVersionUID = 1L;
    private final ProjectionImpl proj;
    private final ReferenceableAxis<Double> xAxis;

    private final ReferenceableAxis<Double> yAxis;
    private final BoundingBox bbox;

    private transient Array2D<GridCell2D> domainObjs = null;

    /**
     * Create a new {@link CdmTransformedGrid} from a defined
     * {@link ProjectionImpl} and a pair of axes
     * 
     * @param proj
     *            The {@link ProjectionImpl} which defines the transforms to and
     *            from lat-lon
     * @param xAxis
     *            The x {@link ReferenceableAxis} of the non-lat-lon grid
     * @param yAxis
     *            The y {@link ReferenceableAxis} of the non-lat-lon grid
     */
    public CdmTransformedGrid(ProjectionImpl proj, ReferenceableAxis<Double> xAxis,
            ReferenceableAxis<Double> yAxis) {
        this.proj = proj;
        this.xAxis = xAxis;
        this.yAxis = yAxis;

        /*
         * Calculate the bounding box from all of the points on the perimeter
         */
        List<HorizontalPosition> perimeter = new ArrayList<>();
        List<Double> xVals = xAxis.getCoordinateValues();
        List<Double> yVals = yAxis.getCoordinateValues();
        for (Double x : xVals) {
            LatLonPoint llp = proj.projToLatLon(x, yVals.get(0));
            perimeter.add(new HorizontalPosition(llp.getLongitude(), llp.getLatitude()));

            llp = proj.projToLatLon(x, yVals.get(yVals.size() - 1));
            perimeter.add(new HorizontalPosition(llp.getLongitude(), llp.getLatitude()));
        }
        for (Double y : yVals) {
            LatLonPoint llp = proj.projToLatLon(xVals.get(0), y);
            perimeter.add(new HorizontalPosition(llp.getLongitude(), llp.getLatitude()));

            llp = proj.projToLatLon(xVals.get(xVals.size() - 1), y);
            perimeter.add(new HorizontalPosition(llp.getLongitude(), llp.getLatitude()));
        }

        this.bbox = GISUtils.getBoundingBox(perimeter);
    }

    /**
     * @param coordSys
     *            This {@link GridCoordSystem} must have one-dimensional x and y
     *            coordinate axes
     */
    public CdmTransformedGrid(GridCoordSystem coordSys) {
        proj = coordSys.getProjection();
        /*
         * If this is a rotated-pole projection then the x axis is longitude and
         * hence wraps at 0/360 degrees.
         */
        boolean xAxisIsLongitude = proj instanceof RotatedPole;
        xAxis = CdmUtils.createReferenceableAxis((CoordinateAxis1D) coordSys.getXHorizAxis(),
                xAxisIsLongitude);
        yAxis = CdmUtils.createReferenceableAxis((CoordinateAxis1D) coordSys.getYHorizAxis());
        LatLonRect latLonBoundingBox = coordSys.getLatLonBoundingBox();
        /*
         * Some projections do not have a well-defined lat-lon bounding box and
         * return NaNs. In these cases, we fall back to using global limits
         */
        double lonMin = latLonBoundingBox.getLonMin();
        double lonMax = latLonBoundingBox.getLonMax();
        double latMin = latLonBoundingBox.getLatMin();
        double latMax = latLonBoundingBox.getLatMax();
        if (Double.isNaN(lonMin)) {
            lonMin = -180.0;
        }
        if (Double.isNaN(lonMax)) {
            lonMax = 180.0;
        }
        if (Double.isNaN(latMin)) {
            latMin = -90.0;
        }
        if (Double.isNaN(latMax)) {
            latMax = 90.0;
        }

        if (latMin != latMax && lonMin != lonMax) {
            /*
             * The normal situation - use the bounds to create the bounding box
             */
            bbox = new BoundingBoxImpl(lonMin, latMin, lonMax, latMax,
                    GISUtils.defaultGeographicCRS());
        } else {
            /*
             * Some projections (MSGnavigation as returned by GRIB is the only
             * one I've actually found so far) end up returning a bounding box
             * with identical latitude (and potentially longitude) values when
             * it's not appropriate.
             *
             * It seems to be the case that the projection just doesn't work.
             */
            bbox = BoundingBoxImpl.global();
        }
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        if (GISUtils.crsMatch(getCoordinateReferenceSystem(),
                position.getCoordinateReferenceSystem())) {
            return xAxis.getCoordinateExtent().contains(position.getX())
                    && yAxis.getCoordinateExtent().contains(position.getY());
        } else {
            HorizontalPosition transformedPosition = GISUtils.transformPosition(position,
                    getCoordinateReferenceSystem());
            return xAxis.getCoordinateExtent().contains(transformedPosition.getX())
                    && yAxis.getCoordinateExtent().contains(transformedPosition.getY());
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bbox;
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() {
        return new DefaultGeographicBoundingBox(bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(),
                bbox.getMaxY());
    }

    @Override
    public long size() {
        return xAxis.size() * yAxis.size();
    }

    @Override
    public Array2D<GridCell2D> getDomainObjects() {
        if (domainObjs == null) {
            domainObjs = new Array2D<GridCell2D>(getYSize(), getXSize()) {
                private static final long serialVersionUID = 1L;

                @Override
                public GridCell2D get(final int... coords) {
                    double x = xAxis.getCoordinateValue(coords[1]);
                    double y = yAxis.getCoordinateValue(coords[0]);
                    /* Translate this point to lon-lat coordinates */
                    LatLonPoint latLon = proj.projToLatLon(x, y);
                    HorizontalPosition centre = new HorizontalPosition(latLon.getLongitude(),
                            latLon.getLatitude());

                    Extent<Double> xExtent = xAxis.getCoordinateBounds(coords[1]);
                    Extent<Double> yExtent = yAxis.getCoordinateBounds(coords[0]);
                    List<HorizontalPosition> vertices = new ArrayList<HorizontalPosition>(4);
                    vertices.add(new HorizontalPosition(xExtent.getLow(), yExtent.getLow()));
                    vertices.add(new HorizontalPosition(xExtent.getHigh(), yExtent.getLow()));
                    vertices.add(new HorizontalPosition(xExtent.getHigh(), yExtent.getHigh()));
                    vertices.add(new HorizontalPosition(xExtent.getLow(), yExtent.getHigh()));
                    final List<HorizontalPosition> iVertices = Collections
                            .unmodifiableList(vertices);

                    Polygon footprint = new SimplePolygon(iVertices) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public CoordinateReferenceSystem getCoordinateReferenceSystem() {
                            return CdmTransformedGrid.this.getCoordinateReferenceSystem();
                        }

                        @Override
                        public boolean contains(double x, double y) {
                            /*
                             * The x,y coordinates are in the external CRS of
                             * this grid
                             */
                            GridCoordinates2D posCoords = CdmTransformedGrid.this.findIndexOf(
                                    new HorizontalPosition(x, y, GISUtils.defaultGeographicCRS()));
                            if (posCoords == null)
                                return false;
                            return (posCoords.getX() == coords[1] && posCoords.getY() == coords[0]);
                        }
                    };

                    return new GridCell2DImpl(new GridCoordinates2D(coords[1], coords[0]), centre,
                            footprint, CdmTransformedGrid.this);
                }
            };
        }
        return domainObjs;
    }

    @Override
    public GridCoordinates2D findIndexOf(HorizontalPosition position) {
        /*
         * Ensure position is in CRS84, so that we can use our cached projection
         * to transform it
         */
        if (!GISUtils.crsMatch(getCoordinateReferenceSystem(),
                position.getCoordinateReferenceSystem())) {
            position = GISUtils.transformPosition(position, getCoordinateReferenceSystem());
        }
        
        if(!this.bbox.contains(position)) {
            /*
             * The position is definitely outside the domain of this grid
             */
            return null;
        }
        
        /*
         * Now transform from CRS84 to the local CRS
         */
        ProjectionPoint transformed = proj.latLonToProj(position.getY(), position.getX());
        /*
         * and find the indices along both axes
         */
        int xi = xAxis.findIndexOf(transformed.getX());
        if(xi < 0) {
            return null;
        }
        int yi = yAxis.findIndexOf(transformed.getY());
        if(yi < 0) {
            return null;
        }
        return new GridCoordinates2D(xi, yi);
    }

    @Override
    public double transformNativeHeadingToWgs84(double xComp, double yComp, double lon,
            double lat) {
        double dxy = 1e-8;
        ProjectionPoint centre = proj.latLonToProj(lat, lon);
        LatLonPoint xPlus = proj.projToLatLon(centre.getX() + dxy, centre.getY());
        LatLonPoint yPlus = proj.projToLatLon(centre.getX(), centre.getY() + dxy);

        /*
         * Java naming convention ignored for clarity that these are partial
         * derivatives
         */
        double dXlatlon_dXnative;
        double dYlatlon_dXnative;
        double dXlatlon_dYnative;
        double dYlatlon_dYnative;
        dXlatlon_dXnative = (xPlus.getLongitude() - lon);
        dYlatlon_dXnative = (xPlus.getLatitude() - lat);

        dXlatlon_dYnative = (yPlus.getLongitude() - lon);
        dYlatlon_dYnative = (yPlus.getLatitude() - lat);

        /*
         * Get the new components
         */
        float newX = (float) (dXlatlon_dXnative * xComp + dXlatlon_dYnative * yComp);
        float newY = (float) (dYlatlon_dXnative * xComp + dYlatlon_dYnative * yComp);

        return GISUtils.RAD2DEG * Math.atan2(newX, newY);
    }

    @Override
    public int getXSize() {
        return xAxis.size();
    }

    @Override
    public int getYSize() {
        return yAxis.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bbox == null) ? 0 : bbox.hashCode());
        result = prime * result + ((proj == null) ? 0 : proj.hashCode());
        result = prime * result + ((xAxis == null) ? 0 : xAxis.hashCode());
        result = prime * result + ((yAxis == null) ? 0 : yAxis.hashCode());
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
        CdmTransformedGrid other = (CdmTransformedGrid) obj;
        if (bbox == null) {
            if (other.bbox != null)
                return false;
        } else if (!bbox.equals(other.bbox))
            return false;
        if (proj == null) {
            if (other.proj != null)
                return false;
        } else if (!proj.getName().equals(other.proj.getName())) {
            return false;
        } else if (!proj.getProjectionParameters().equals(other.proj.getProjectionParameters())) {
            return false;
        }
        if (xAxis == null) {
            if (other.xAxis != null)
                return false;
        } else if (!xAxis.equals(other.xAxis))
            return false;
        if (yAxis == null) {
            if (other.yAxis != null)
                return false;
        } else if (!yAxis.equals(other.yAxis))
            return false;
        return true;
    }

}
