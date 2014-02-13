package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;

public class GridCell2DImplTest {
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
    public void testContains() {
        Array<GridCell2D> cells = rGrid.getDomainObjects();
        GridCell2D cell = cells.get(0, 0);
        HorizontalPosition position = new HorizontalPosition(120.0, 30.0, crs);
        assertFalse(cell.contains(position));
        assertFalse(cell.contains(null));

        cell = cells.get(3, 4);
        position = new HorizontalPosition(97.2, 34.444, crs);
        assertTrue(cell.contains(position));
        assertEquals(rGrid, cell.getGrid());
    }

    @Test
    public void testGetFoorprint() {
        Array<GridCell2D> cells = rGrid.getDomainObjects();
        GridCell2D cell = cells.get(1, 4);
        BoundingBox bbox = new BoundingBoxImpl(96.75, 33.25, 97.25, 33.75, crs);
        assertEquals(cell.getFootprint(), bbox);

        cell = cells.get(3, 4);
        bbox = new BoundingBoxImpl(96.75, 33.6, 97.25, 33.98, crs);
        assertFalse(cell.getFootprint().equals(bbox));
    }
}
