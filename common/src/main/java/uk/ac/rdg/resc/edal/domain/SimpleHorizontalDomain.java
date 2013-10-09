package uk.ac.rdg.resc.edal.domain;

import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * A simple implementation of a {@link HorizontalDomain} where all co-ordinates
 * are in the WGS84 CRS
 * 
 * @author Guy Griffiths
 */
public class SimpleHorizontalDomain implements HorizontalDomain {

    private final BoundingBox bbox;

    /**
     * Create a {@link HorizontalDomain} based on a WGS84 bounding box
     * 
     * @param minLon
     *            The minimum longitude
     * @param minLat
     *            The minimum latitude
     * @param maxLon
     *            The maximum longitude
     * @param maxLat
     *            The maximum latitude
     */
    public SimpleHorizontalDomain(double minLon, double minLat, double maxLon, double maxLat) {
        bbox = new BoundingBoxImpl(minLon, minLat, maxLon, maxLat, DefaultGeographicCRS.WGS84);
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return bbox.contains(position);
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
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return DefaultGeographicCRS.WGS84;
    }
}
