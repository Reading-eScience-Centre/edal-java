package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

public class RectilinearGridImplTest {
    private RectilinearGrid rGrid;
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

    @Before
    public void setUp() throws Exception {
        double resolution = 1.0 / 2;
        ReferenceableAxis<Double> longAxis = new RegularAxisImpl("longitude", 95.0, resolution, 12,
                true);
        ReferenceableAxis<Double> latAxis = new RegularAxisImpl("latitude", 33.0, resolution, 16,
                false);
        rGrid = new RectilinearGridImpl(longAxis, latAxis, crs);
    }

    @Test
    public void testContains() throws Exception {
        HorizontalPosition position = new HorizontalPosition(120, 30, crs);
        assertFalse(rGrid.contains(position));
        position = new HorizontalPosition(96.0, 34.6, crs);
        assertTrue(rGrid.contains(position));
        position = new HorizontalPosition(96.0, 40.7, crs);
        assertTrue(rGrid.contains(position));
        assertFalse(rGrid.contains(null));
        CoordinateReferenceSystem japanArea = GISUtils.getCrs("EPSG:2450");
        assertFalse(rGrid.contains(new HorizontalPosition(17945.194292, 41625.344542, japanArea)));
    }

    @Test
    public void testFindIndexOf() {
        HorizontalPosition position = new HorizontalPosition(95.3, 34.35, crs);
        GridCoordinates2D coord = new GridCoordinates2D(1, 3);
        assertEquals(coord, rGrid.findIndexOf(position));

        position = new HorizontalPosition(100.4, 40.7, crs);
        coord = new GridCoordinates2D(11, 15);
        assertEquals(coord, rGrid.findIndexOf(position));

    }
}
