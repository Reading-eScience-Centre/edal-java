package uk.ac.rdg.resc.edal.grid;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.SimpleGridDomain;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalCrsImpl;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.Array;
import uk.ac.rdg.resc.edal.util.Extents;

public class SimpleGridCell4DTest {
    private SimpleGridDomain sgd;
    private Extent<Double> vExtent;
    private Extent<DateTime> tExtent;
    private CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
    private VerticalCrs depth = new VerticalCrsImpl("meter", false, false, false);
    private Chronology chronology = ISOChronology.getInstance();

    @Before
    public void setUp() throws Exception {
        double resolution = 1.0;
        ReferenceableAxis<Double> longAxis = new RegularAxisImpl("longitude", -9.5, resolution, 10,
                true);
        ReferenceableAxis<Double> latAxis = new RegularAxisImpl("latitude", 50.5, resolution, 8,
                false);
        RectilinearGrid rGrid = new RectilinearGridImpl(longAxis, latAxis, crs);

        vExtent = Extents.newExtent(0.0, 1000.0);
        DateTime begin = new DateTime(2000, 1, 1, 0, 0, chronology);
        tExtent = Extents.newExtent(begin, begin.plusDays(10));

        List<Double> vValues = new ArrayList<Double>();
        for (int i = 0; i < 11; i++) {
            vValues.add(i * 100.0);
        }
        VerticalAxis vAxis = new VerticalAxisImpl("depth", vValues, depth);

        List<DateTime> datetimes = new ArrayList<DateTime>();
        for (int i = 0; i < 11; i++) {
            DateTime dt = begin.plusDays(i);
            datetimes.add(dt);
        }
        TimeAxis tAxis = new TimeAxisImpl("Sample TimeAxis", datetimes);
        sgd = new SimpleGridDomain(rGrid, vAxis, tAxis);
    }

    @Test
    public void testContains() {
        HorizontalPosition hPos = new HorizontalPosition(-6.5, 52.7, crs);
        VerticalPosition vPos = new VerticalPosition(100.9, depth);
        DateTime dt = new DateTime(2000, 1, 2, 12, 45, chronology);

        Array<GridCell2D> cells = sgd.getHorizontalGrid().getDomainObjects();
        GridCell2D cell = cells.get(2, 3);
        SimpleGridCell4D sgc = new SimpleGridCell4D(cell, vExtent, depth, tExtent, chronology, sgd);

        GeoPosition gPos = new GeoPosition(hPos, vPos, dt);
        assertTrue(sgc.contains(gPos));
        assertFalse(sgc.contains(null));

        vPos = new VerticalPosition(1000.9, depth);
        gPos = new GeoPosition(hPos, vPos, dt);
        assertFalse(sgc.contains(gPos));

        vPos = new VerticalPosition(800.9, depth);
        dt = new DateTime(2000, 11, 12, 2, 45, chronology);
        gPos = new GeoPosition(hPos, vPos, dt);
        assertFalse(sgc.contains(gPos));

        hPos = new HorizontalPosition(-8.5, 53.7, crs);
        vPos = new VerticalPosition(100.9, depth);
        dt = new DateTime(2000, 1, 2, 12, 45, chronology);
        gPos = new GeoPosition(hPos, vPos, dt);
        assertFalse(sgc.contains(gPos));

        cell = null;
        sgc = new SimpleGridCell4D(cell, vExtent, depth, tExtent, chronology, sgd);
        assertFalse(sgc.contains(gPos));
    }
}
