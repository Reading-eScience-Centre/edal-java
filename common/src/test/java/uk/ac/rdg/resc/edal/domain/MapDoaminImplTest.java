package uk.ac.rdg.resc.edal.domain;

import static org.junit.Assert.*;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.RectilinearGridImpl;
import uk.ac.rdg.resc.edal.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.grid.RegularAxisImpl;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

public class MapDoaminImplTest {
    private MapDomain mapdomain;
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

    // grid resolution
    double resolution = 1.0 / 2;
    // grid x-side cell number
    int xSize = 6;
    // grid y-side cell number
    int ySize = 8;
    // longitude position of lower left point of the HorizontalGrid
    double lowX = -10.0;
    // latitude position of lower left point of the HorizontalGrid
    double lowY = 51.0;
    private HorizontalGrid hGrid;

    @Before
    public void setUp() throws Exception {
        double z = 100.0;
        // m stands for meter
        VerticalCrs vCrs = new VerticalCrsImpl("m", false, false, false);
        Chronology chrnology = ISOChronology.getInstance();
        DateTime time = new DateTime(2000, 01, 01, 00, 00, chrnology);
        ReferenceableAxis<Double> longAxis = new RegularAxisImpl("longitude", -10.0, resolution,
                xSize, true);
        ReferenceableAxis<Double> latAxis = new RegularAxisImpl("latitude", 51.0, resolution,
                ySize, false);
        hGrid = new RectilinearGridImpl(longAxis, latAxis, crs);
        mapdomain = new MapDomainImpl(hGrid, z, vCrs, time);
    }

    @Test
    public void test() {
        assertEquals(hGrid.getXSize() * hGrid.getYSize(), mapdomain.size());

        HorizontalPosition hPos = new HorizontalPosition(20.3, 60.8, crs);
        assertFalse(mapdomain.contains(hPos));

        hPos = new HorizontalPosition(-9.3, 53.2, crs);
        assertTrue(mapdomain.contains(hPos));

        int expectedXPos = (int) ((hPos.getX() - lowX) / resolution);
        int expectedYPos = (int) ((hPos.getY() - lowY) / resolution);
        GridCoordinates2D expectedCoordinate = new GridCoordinates2D(expectedXPos, expectedYPos);
        assertEquals(expectedCoordinate, mapdomain.findIndexOf(hPos));

        Extent<Double> xExtent = Extents.newExtent(lowX - resolution / 2, lowX - resolution / 2
                + xSize * resolution);
        Extent<Double> yExtent = Extents.newExtent(lowY - resolution / 2, lowY - resolution / 2
                + ySize * resolution);
        BoundingBox expectedBbox = new BoundingBoxImpl(xExtent, yExtent, crs);
        assertEquals(expectedBbox, mapdomain.getBoundingBox());

        Array<GridCell2D> cells = mapdomain.getDomainObjects();
        for (GridCell2D cell : cells) {
            int xIndex = cell.getGridCoordinates().getX();
            int yIndex = cell.getGridCoordinates().getY();
            HorizontalPosition expectedcentre = new HorizontalPosition(lowX + xIndex * resolution,
                    lowY + yIndex * resolution, crs);
            assertEquals(expectedcentre, cell.getCentre());
            assertEquals(hGrid, cell.getGrid());
        }
    }
}
