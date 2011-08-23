package uk.ac.rdg.resc.edal.position.impl;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.position.LonLatPosition;

/**
 * Immutable implementation of {@link LonLatPosition}.
 * 
 * @author Jon
 */
public final class LonLatPositionImpl extends HorizontalPositionImpl implements LonLatPosition {

    /**
     * Returns the longitude, in the range [-180:180] degrees.
     * 
     * @return the longitude, in the range [-180:180] degrees.
     */
    @Override
    public double getLongitude() {
        return this.getX();
    }

    /**
     * Returns the geodetic latitude in degrees.
     * 
     * @return the geodetic latitude in degrees.
     */
    @Override
    public double getLatitude() {
        return this.getY();
    }

    /**
     * Creates a new LonLatPositionImpl with the given coordinates.
     * 
     * @param longitude
     *            The longitude. Will be converted internally to a longitude in
     *            the range [-180:180], so all getter methods will return values
     *            in this range.
     * @param latitude
     *            The geodetic latitude
     */
    public LonLatPositionImpl(double longitude, double latitude, CoordinateReferenceSystem crs) {
        super(longitude, latitude, crs);
    }

    /**
     * Creates a new LonLatPositionImpl with the given coordinates.
     * 
     * @param longitude
     *            The longitude. Will be converted internally to a longitude in
     *            the range [-180:180], so all getter methods will return values
     *            in this range.
     * @param latitude
     *            The geodetic latitude
     */
    public LonLatPositionImpl(double longitude, double latitude) {
        super(longitude, latitude);
    }
}
