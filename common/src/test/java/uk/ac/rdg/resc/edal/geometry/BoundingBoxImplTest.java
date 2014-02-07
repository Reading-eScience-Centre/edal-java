package uk.ac.rdg.resc.edal.geometry;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import uk.ac.rdg.resc.edal.position.*;
import uk.ac.rdg.resc.edal.util.*;

public class BoundingBoxImplTest {
    private BoundingBoxImpl bbox;

    @Before
    public void setUp() throws Exception {
        bbox = new BoundingBoxImpl(137.47, -20.0, 180.0, 60.0, DefaultGeographicCRS.WGS84);
    }

    @Test
    public void testContains() throws Exception {
        assertTrue(bbox.contains(160.0, -20.0));
        assertTrue(bbox.contains(150.0, 40.0));

        assertTrue(bbox.contains(137.47, -20.0));
        assertTrue(bbox.contains(137.47, 40.0));
        assertTrue(bbox.contains(137.47, 60.0));
        assertTrue(bbox.contains(150.0, -20.0));

        assertTrue(bbox.contains(180.0, -20.0));
        assertTrue(bbox.contains(180.0, 60.0));
        assertTrue(bbox.contains(180.0, 30.0));
        assertTrue(bbox.contains(160.0, 60.0));

        assertFalse(bbox.contains(190, 0.0));
        assertFalse(bbox.contains(Double.NaN, -30.0));

        assertFalse(bbox.contains(null));

        CoordinateReferenceSystem japanArea = GISUtils.getCrs("EPSG:2450");
        assertTrue(bbox.contains(new HorizontalPosition(17945.194292, 41625.344542, japanArea)));

        CoordinateReferenceSystem chinaArea = GISUtils.getCrs("EPSG:2426");
        assertFalse(bbox.contains(new HorizontalPosition(500000, 4261964.001513, chinaArea)));
    }

}
