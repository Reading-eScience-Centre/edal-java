package uk.ac.rdg.resc.edal.cdm;

import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.impl.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.LonLatPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;
import uk.ac.rdg.resc.edal.position.impl.LonLatPositionImpl;

public class Utils extends uk.ac.rdg.resc.edal.util.Utils {

    /**
     * Converts the given GeographicBoundingBox to a BoundingBox in WGS84
     * longitude-latitude coordinates. This method assumes that the longitude
     * and latitude coordinates in the given GeographicBoundingBox are in the
     * WGS84 system (this is not always true: GeographicBoundingBoxes are often
     * approximate and in no specific CRS).
     */
    public static BoundingBox getBoundingBox(GeographicBoundingBox geoBbox) {
        return new BoundingBoxImpl(new double[] { geoBbox.getWestBoundLongitude(), geoBbox.getSouthBoundLatitude(),
                geoBbox.getEastBoundLongitude(), geoBbox.getNorthBoundLatitude() }, DefaultGeographicCRS.WGS84);
    }

    /**
     * Transforms the given HorizontalPosition to a longitude-latitude position
     * in the WGS84 coordinate reference system.
     * 
     * @param pos
     *            The position to translate
     * @param targetCrs
     *            The CRS to translate into
     * @return a new position in the given CRS, or the same position if the new
     *         CRS is the same as the point's CRS. The returned point's CRS will
     *         be set to {@code targetCrs}.
     * @throws NullPointerException
     *             if {@code pos.getCoordinateReferenceSystem()} is null
     * @todo refactor to share code with above method?
     */
    public static LonLatPosition transformToWgs84LonLat(HorizontalPosition pos) {
        if (pos instanceof LonLatPosition)
            return (LonLatPosition) pos;
        CoordinateReferenceSystem sourceCrs = pos.getCoordinateReferenceSystem();
        if (sourceCrs == null) {
            throw new NullPointerException("Position must have a valid CRS");
        }
        // CRS.findMathTransform() caches recently-used transform objects so
        // we should incur no large penalty for multiple invocations
        try {
            MathTransform transform = CRS.findMathTransform(sourceCrs, DefaultGeographicCRS.WGS84);
            if (transform.isIdentity())
                return new LonLatPositionImpl(pos.getX(), pos.getY());
            double[] point = new double[] { pos.getX(), pos.getY() };
            transform.transform(point, 0, point, 0, 1);
            return new LonLatPositionImpl(point[0], point[1]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transforms the given HorizontalPosition to a new position in the given
     * coordinate reference system.
     * 
     * @param pos
     *            The position to translate
     * @param targetCrs
     *            The CRS to translate into
     * @return a new position in the given CRS, or the same position if the new
     *         CRS is the same as the point's CRS. The returned point's CRS will
     *         be set to {@code targetCrs}.
     * @throws NullPointerException
     *             if {@code pos.getCoordinateReferenceSystem()} is null, or if
     *             {@code targetCrs} is null.
     * @todo error handling
     */
    public static HorizontalPosition transformPosition(HorizontalPosition pos, CoordinateReferenceSystem targetCrs) {
        CoordinateReferenceSystem sourceCrs = pos.getCoordinateReferenceSystem();
        if (sourceCrs == null) {
            throw new NullPointerException("Position must have a valid CRS");
        }
        if (targetCrs == null) {
            throw new NullPointerException("Target CRS cannot be null");
        }
        // CRS.findMathTransform() caches recently-used transform objects so
        // we should incur no large penalty for multiple invocations
        try {
            MathTransform transform = CRS.findMathTransform(sourceCrs, targetCrs);
            if (transform.isIdentity())
                return pos;
            double[] point = new double[] { pos.getX(), pos.getY() };
            transform.transform(point, 0, point, 0, 1);
            return new HorizontalPositionImpl(point[0], point[1], targetCrs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the CRS is a WGS84 longitude-latitude system (with the
     * longitude axis first).
     * 
     * @param crs
     * @return
     */
    public static boolean isWgs84LonLat(CoordinateReferenceSystem crs) {
        try {
            return CRS.findMathTransform(crs, DefaultGeographicCRS.WGS84).isIdentity();
        } catch (Exception e) {
            return false;
        }
    }
}
