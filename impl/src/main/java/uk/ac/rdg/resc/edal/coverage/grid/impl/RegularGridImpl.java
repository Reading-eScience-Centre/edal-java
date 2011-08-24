package uk.ac.rdg.resc.edal.coverage.grid.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;

import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;

/**
 * Immutable implementation of a {@link RegularGrid}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public final class RegularGridImpl extends AbstractRectilinearGrid implements RegularGrid {
    private final RegularAxis xAxis;
    private final RegularAxis yAxis;

    public RegularGridImpl(RegularAxis xAxis, RegularAxis yAxis, CoordinateReferenceSystem crs) {
        super(crs);
        if (xAxis == null || yAxis == null) {
            throw new NullPointerException("Axes cannot be null");
        }
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    /**
     * Constructs a RegularGrid with a null coordinate reference system
     */
    public RegularGridImpl(RegularAxis xAxis, RegularAxis yAxis) {
        this(xAxis, yAxis, null);
    }

    /**
     * <p>
     * Constructs a RegularGrid from the given bounding box, with the given
     * width and height. Note that the bounding box represents the edges of the
     * grid, whereas grid coordinates represent the centre of the grid points.
     * </p>
     * 
     * <p>
     * The coordinate reference system of the returned grid will be taken from
     * the CRS of the bounding box. If this is null, the names of the axes will
     * be set to "Unknown X axis" and "Unknown Y axis".
     * </p>
     * 
     * @param bbox
     *            The bounding box of the grid
     * @param width
     *            the number of grid points in the x direction
     * @param height
     *            the number of grid points in the y direction
     */
    public RegularGridImpl(BoundingBox bbox, int width, int height) {
        this(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(), bbox.getCoordinateReferenceSystem(),
                width, height);
    }

    /**
     * @param bbox
     *            [minx, miny, maxx, maxy]
     * @param crs
     * @param width
     *            the number of grid points in the x direction
     * @param height
     *            the number of grid points in the y direction
     */
    public RegularGridImpl(double[] bbox, CoordinateReferenceSystem crs, int width, int height) {
        this(bbox[0], bbox[1], bbox[2], bbox[3], crs, width, height);
    }

    public RegularGridImpl(double minx, double miny, double maxx, double maxy, CoordinateReferenceSystem crs,
            int width, int height) {
        super(crs);
        if (maxx < minx || maxy < miny) {
            throw new IllegalArgumentException("Invalid bounding box");
        }

        double xSpacing = (maxx - minx) / width;
        double ySpacing = (maxy - miny) / height;

        // The axis values represent the centres of the grid points
        double firstXAxisValue = minx + (0.5 * xSpacing);
        double firstYAxisValue = miny + (0.5 * ySpacing);

        // TODO: identify whether the axis is longitude
        // Can we use axis.rangemeaning == WRAPS for this? Do we also have
        // to check that the units of the axis are correct (degrees rather than
        // radians) and that the axis is really longitude?
        boolean isLongitude = false;

        if (crs == null) {
            xAxis = new RegularAxisImpl("Unknown X axis", firstXAxisValue, xSpacing, width, isLongitude);
            // y axis is very unlikely to be longitude
            yAxis = new RegularAxisImpl("Unknown Y axis", firstYAxisValue, ySpacing, height, false);
        } else {
            CoordinateSystem cs = crs.getCoordinateSystem();
            xAxis = new RegularAxisImpl(cs.getAxis(0), firstXAxisValue, xSpacing, width, isLongitude);
            // y axis is very unlikely to be longitude
            yAxis = new RegularAxisImpl(cs.getAxis(1), firstYAxisValue, ySpacing, height, false);
        }
    }

    @Override
    public RegularAxis getAxis(int index) {
        if (index == 0)
            return getXAxis();
        if (index == 1)
            return getYAxis();
        throw new IndexOutOfBoundsException();
    }

    @Override
    public RegularAxis getXAxis() {
        return xAxis;
    }

    @Override
    public RegularAxis getYAxis() {
        return yAxis;
    }
}
