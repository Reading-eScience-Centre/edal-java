package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

public class RectilinearGridImplTest {
    private int xSize = 12;
    private int ySize = 16;
    private RectilinearGrid rGrid;
    ReferenceableAxis<Double> longAxis;
    ReferenceableAxis<Double> latAxis;
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

    @Before
    public void setUp() throws Exception {
        double resolution = 1.0 / 2;
        longAxis = new RegularAxisImpl("longitude", 95.0, resolution, xSize, true);
        latAxis = new RegularAxisImpl("latitude", 33.0, resolution, ySize, false);
        rGrid = new RectilinearGridImpl(longAxis, latAxis, crs);
    }

    @Test
    public void testGetMethods() {
        assertEquals(xSize, rGrid.getXSize());
        assertEquals(ySize, rGrid.getYSize());
        assertEquals(xSize * ySize, rGrid.size());
        assertEquals(longAxis, rGrid.getXAxis());
        assertEquals(latAxis, rGrid.getYAxis());
        assertEquals(crs, rGrid.getCoordinateReferenceSystem());
        BoundingBox bbox = new BoundingBoxImpl(94.75, 32.75, 100.75, 40.75, crs);
        assertEquals(bbox, rGrid.getBoundingBox());

        DefaultGeographicBoundingBox gbbox = new DefaultGeographicBoundingBox(94.75, 100.75, 32.75,
                40.75);
        assertEquals(gbbox, rGrid.getGeographicBoundingBox());

        List<HorizontalPosition> hPoss = new ArrayList<>(xSize * ySize);
        for (int i = 0; i < ySize; i++) {
            for (int j = 0; j < xSize; j++) {
                hPoss.add(new HorizontalPosition(95.0 + j * 0.5, 33.0 + i * 0.5, crs));
            }
        }

        Array<GridCell2D> cells = rGrid.getDomainObjects();
        for (GridCell2D cell : cells) {
            int xIndex = cell.getGridCoordinates().getX();
            int yIndex = cell.getGridCoordinates().getY();
            assertEquals(hPoss.get(xIndex + yIndex * xSize), cell.getCentre());
            assertEquals(rGrid, cell.getGrid());
        }
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
